package com.invictus.hushbro.pipeline

import android.util.Log
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.prefs.UserSettings
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.di.ApplicationScope
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.util.TextHash
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPipeline @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val notificationRepository: NotificationRepository,
    private val listenerBridge: ListenerBridge,
    private val settingsStore: SettingsStore? = null,
    private val redactor: Redactor,
    private val ruleEngine: RuleEngine,
    private val classifier: Classifier
) {
    companion object {
        private const val TAG = "HushBroPipeline"
        const val CHANNEL_CAPACITY = 512
        const val MAX_BATCH_SIZE = 100
        const val DRAIN_TIMEOUT_MS = 250L
    }

    private val channel = Channel<RawNotification>(CHANNEL_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val consumerStarted = AtomicBoolean(false)

    fun start() = Unit

    fun enqueue(raw: RawNotification): Boolean {
        val accepted = channel.trySend(raw).isSuccess
        if (consumerStarted.compareAndSet(false, true)) appScope.launch { consumeLoop() }
        return accepted
    }

    private suspend fun consumeLoop() {
        while (true) {
            try {
                val first = channel.receiveCatching()
                if (!first.isSuccess) return
                val batch = mutableListOf(first.getOrThrow())
                withTimeoutOrNull(DRAIN_TIMEOUT_MS) {
                    while (batch.size < MAX_BATCH_SIZE) {
                        val next = channel.receiveCatching().getOrNull() ?: break
                        batch.add(next)
                    }
                }
                processBatch(batch)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Throwable) {
                runCatching { Log.w(TAG, "Notification batch failed", e) }
            }
        }
    }

    private suspend fun processBatch(batch: List<RawNotification>) {
        val settings = settingsStore?.userSettingsFlow?.first() ?: UserSettings()
        val quietHoursActive = isWithinQuietHours(settings)
        val entities = mutableListOf<NotificationEntity>()
        var failed = 0

        for (raw in batch) {
            try {
                val redacted = redactor.redact(raw)
                val tag = classifier.classify(raw, redacted)
                val action = if (raw.rulesEvaluated) raw.preEvaluatedAction else ruleEngine.evaluate(raw, redacted)
                val status = when {
                    action == RuleAction.MUTE -> NotificationStatus.MUTED
                    quietHoursActive -> NotificationStatus.HELD
                    else -> NotificationStatus.KEPT
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
                        isImportant = action == RuleAction.MARK_IMPORTANT,
                        textHash = TextHash.of(redacted.title, redacted.text)
                    )
                )
                // Real listener callbacks cancel matching keys immediately. Keep the delayed fallback only for non-listener callers/tests.
                if (action == RuleAction.MUTE && !raw.listenerCallbackHandled) listenerBridge.cancelNotificationByKey(raw.key)
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                failed++
            }
        }
        if (failed > 0) runCatching { Log.w(TAG, "Skipped $failed notification(s) due to processing errors") }
        if (entities.isNotEmpty()) {
            try {
                notificationRepository.insertBatch(entities)
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                runCatching { Log.w(TAG, "Notification batch insert failed", e) }
            }
        }
    }

    private fun isWithinQuietHours(settings: UserSettings): Boolean {
        if (!settings.quietHoursEnabled) return false
        val calendar = Calendar.getInstance()
        val now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val start = settings.quietHoursStart.coerceIn(0, 1439)
        val end = settings.quietHoursEnd.coerceIn(0, 1439)
        if (start == end) return false
        return if (start < end) now >= start && now < end else now >= start || now < end
    }
}
