package com.invictus.hushbro.pipeline

import android.util.Log
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.di.ApplicationScope
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.util.TextHash
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Buffered notification processing pipeline and batch writer.
 *
 * The notification listener callback stays tiny: it extracts minimal fields and
 * calls [enqueue], which uses [Channel.trySend] and never suspends. A single
 * consumer coroutine runs on the application scope (which embeds
 * [kotlinx.coroutines.Dispatchers.Default]) and:
 *
 *  1. receives one item, then drains up to [MAX_BATCH_SIZE] items or
 *     [DRAIN_TIMEOUT_MS] milliseconds, whichever comes first;
 *  2. processes each item off the callback thread (redact -> classify ->
 *     evaluate rules -> stable text hash -> entity);
 *  3. writes the whole batch with one transaction-backed repository insertBatch.
 *
 * The channel is bounded ([CHANNEL_CAPACITY]) with a DROP_OLDEST overflow
 * policy, so notification bursts cannot grow memory without bound and producers
 * never block.
 *
 * When a MUTE rule applies, [ListenerBridge.cancelNotificationByKey] is used to
 * ask the connected listener service to remove the original shade notification.
 */
@Singleton
class NotificationPipeline @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val notificationRepository: NotificationRepository,
    private val listenerBridge: ListenerBridge,
    private val redactor: Redactor,
    private val ruleEngine: RuleEngine,
    private val classifier: Classifier
) {

    companion object {
        private const val TAG = "HushBroPipeline"

        /** Bounded buffer capacity: on overflow the oldest pending item is dropped. */
        const val CHANNEL_CAPACITY = 512

        /** Maximum size of a single batch written to the database. */
        const val MAX_BATCH_SIZE = 100

        /** How long the consumer keeps draining after the first item. */
        const val DRAIN_TIMEOUT_MS = 250L
    }

    private val channel = Channel<RawNotification>(
        capacity = CHANNEL_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var started: Boolean = false
    private var consumerJob: kotlinx.coroutines.Job? = null

    private val consumerStarted = AtomicBoolean(false)

    /**
     * Starts the single consumer coroutine on the application scope. Idempotent
     * and safe to call from any thread; production invocations run on
     * Dispatchers.Default because the application scope embeds it.
     */
    fun start() {
        // Consumer is started lazily on first enqueue.
    }

    /**
     * Buffers a raw notification for off-thread processing. Never suspends:
     * returns `true` when the item was accepted into the channel, `false` if the
     * channel is closed/cancelled. On overflow the oldest pending item is
     * dropped (bounded, DROP_OLDEST).
     */
    fun enqueue(raw: RawNotification): Boolean {
        val result = channel.trySend(raw).isSuccess
        if (consumerStarted.compareAndSet(false, true)) {
            appScope.launch { consumeLoop() }
        }
        return result
    }

    private suspend fun consumeLoop() {
        while (true) {
            try {
                val firstResult = channel.receiveCatching()
                if (!firstResult.isSuccess) return // channel closed/cancelled
                val batch = mutableListOf(firstResult.getOrThrow())
                // Drain up to MAX_BATCH_SIZE items or DRAIN_TIMEOUT_MS, whichever
                // comes first. The timeout is enforced with withTimeoutOrNull so a
                // quiet channel is not busy-polled.
                withTimeoutOrNull(DRAIN_TIMEOUT_MS) {
                    while (batch.size < MAX_BATCH_SIZE) {
                        val next = channel.receiveCatching().getOrNull()
                        if (next == null) break // channel closed/cancelled
                        batch.add(next)
                    }
                }
                processBatch(batch)
            } catch (cancelled: CancellationException) {
                throw cancelled // app-scope shutdown: terminate the consumer cleanly
            } catch (exception: Throwable) {
                logWarning("Batch failed; continuing to the next batch", exception)
            }
        }
    }
    private suspend fun processBatch(batch: List<RawNotification>) {
        println("PROCESS_START size=${batch.size}")
        val entities = mutableListOf<NotificationEntity>()
        var failedItems = 0
        for (raw in batch) {
            try {
                println("PROCESS_ITEM key=${raw.key}")
                val redacted = redactor.redact(raw)
                val tag = classifier.classify(raw, redacted)
                val ruleAction = ruleEngine.evaluate(raw, redacted)
                val textHash = TextHash.of(redacted.title, redacted.text)
                val status = if (ruleAction == RuleAction.MUTE) {
                    NotificationStatus.MUTED
                } else {
                    NotificationStatus.KEPT
                }
                entities.add(
                    NotificationEntity(
                        key = raw.key,
                        packageName = raw.packageName,
                        appLabel = raw.appLabel,
                        title = redacted.title,
                        text = redacted.text,
                        postTime = raw.postTime,
                        tag = tag,
                        status = status,
                        textHash = textHash
                    )
                )
                if (ruleAction == RuleAction.MUTE) {
                    listenerBridge.cancelNotificationByKey(raw.key)
                }
            } catch (exception: Throwable) {
                // One bad item must never take the whole pipeline down. Only a
                // safe count is logged; raw content is never logged.
                failedItems++
            }
        }
        if (failedItems > 0) {
            logWarning("Skipped $failedItems of ${batch.size} item(s) due to processing errors")
        }
        if (entities.isEmpty()) return
        try {
            notificationRepository.insertBatch(entities)
        } catch (exception: Throwable) {
            // The transaction rolled back; the items are lost for this batch.
            // Active notifications are re-enqueued on the next listener
            // reconnect, so the unique index never creates duplicate history.
            logWarning("Batch insert of ${entities.size} row(s) failed", exception)
        }
    }
    private fun logWarning(message: String, throwable: Throwable? = null) {
        try {
            if (throwable == null) {
                Log.w(TAG, message)
            } else {
                Log.w(TAG, message, throwable)
            }
        } catch (ignored: Throwable) {
            // Logging is unavailable in this environment; never let it break processing.
        }
    }

}