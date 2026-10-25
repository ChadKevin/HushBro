package com.invictus.hushbro.pipeline

import com.google.common.truth.Truth.assertThat
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.AppSummary
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.service.ListenerBridge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for the Step 5 buffered pipeline and batch writer.
 *
 * Most tests use [UnconfinedTestDispatcher] so that the consumer coroutine
 * runs eagerly when items are enqueued, making assertions deterministic.
 * Tests that verify timing-sensitive behaviour (batch splitting, DROP_OLDEST
 * overflow) use [StandardTestDispatcher] so items accumulate in the channel
 * before the consumer runs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationPipelineTest {

    /**
     * Fake DAO that additionally emulates the Room unique index
     * `(key, postTime, textHash)` with `OnConflictStrategy.IGNORE` semantics:
     * an insert whose identity already exists is skipped and is not persisted.
     */
    private class DedupFakeDao : NotificationDao {
        val stored = mutableListOf<NotificationEntity>()
        val insertBatchSizes = mutableListOf<Int>()
        private val seen = mutableSetOf<String>()

        override suspend fun insertBatch(notifications: List<NotificationEntity>): List<Long> {
            insertBatchSizes.add(notifications.size)
            val ids = mutableListOf<Long>()
            for (n in notifications) {
                val identity = "${n.key}|${n.postTime}|${n.textHash}"
                when (seen.add(identity)) {
                    true -> {
                        stored.add(n)
                        ids.add(stored.size.toLong())
                    }
                    else -> ids.add(-1L)
                }
            }
            return ids
        }

        override suspend fun insert(notification: NotificationEntity): Long {
            stored.add(notification)
            return stored.size.toLong()
        }

        override fun searchPaged(query: String) = throw NotImplementedError()

        override fun search(query: String) = flowOf(stored.filter { it.title?.contains(query) == true })

        override fun getPagedByApp(packageName: String) = throw NotImplementedError()

        override fun getByApp(packageName: String) = flowOf(stored.filter { it.packageName == packageName })

        override fun getAppSummaries() = flowOf(emptyList<AppSummary>())

        override suspend fun markRead(id: Long) {}

        override suspend fun markAllRead() {}

        override suspend fun markAppRead(packageName: String) {}

        override suspend fun clearAll() {
            stored.clear()
        }

        override suspend fun clearApp(packageName: String) {
            stored.removeAll { it.packageName == packageName }
        }

        override suspend fun updateStatus(id: Long, status: NotificationStatus) {}

        override suspend fun updateStatusBatch(ids: List<Long>, status: NotificationStatus) {}

        override suspend fun getHeldForDigest(upToTime: Long) = emptyList<NotificationEntity>()

        override suspend fun deleteById(id: Long) {}

        override suspend fun deleteOlderThan(timestamp: Long): Int = 0

        override suspend fun getById(id: Long): NotificationEntity? = null

        override suspend fun getCount(): Int = stored.size

        override fun getAllFlow() = flowOf(stored.toList())
    }

    /** Records cancellation requests instead of touching the Android service. */
    private class RecordingListenerBridge : ListenerBridge() {
        val cancelledKeys = mutableListOf<String>()

        override fun cancelNotificationByKey(key: String) {
            cancelledKeys.add(key)
        }
    }

    /** Rule engine stub that mutes a single key, simulating a real MUTE rule match. */
    private class MuteKeyRuleEngine(private val muteKey: String) : RuleEngine {
        override fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction? =
            if (raw.key == muteKey) RuleAction.MUTE else null
    }

    /** Redactor stub that fails for a fixed key, simulating a per-item processing error. */
    private class FailingRedactor(private val failKey: String) : Redactor {
        override fun redact(raw: RawNotification): RedactedNotification {
            if (raw.key == failKey) throw IllegalStateException("simulated processing failure")
            return RedactedNotification(title = raw.title, text = raw.text)
        }
    }

    private fun rawNotification(
        key: String = "key",
        postTime: Long = 1000L,
        title: String? = "Title",
        text: String? = "Text"
    ): RawNotification = RawNotification(
        key = key,
        packageName = "com.example.app",
        appLabel = "ExampleApp",
        title = title,
        text = text,
        postTime = postTime
    )

    /**
     * Creates a test pipeline backed by [UnconfinedTestDispatcher] so the
     * consumer coroutine runs eagerly when items are enqueued, making
     * assertions deterministic without manual scheduler advancement.
     */
    private fun pipeline(
        dao: NotificationDao,
        bridge: ListenerBridge = ListenerBridge(),
        redactor: Redactor = PassThroughRedactor(),
        ruleEngine: RuleEngine = PassThroughRuleEngine(),
        useStandardDispatcher: Boolean = false
    ): Pair<NotificationPipeline, TestScope> {
        val scope = if (useStandardDispatcher) {
            TestScope(StandardTestDispatcher())
        } else {
            TestScope(UnconfinedTestDispatcher())
        }
        val pipeline = NotificationPipeline(
            scope,
            NotificationRepository(dao),
            bridge,
            null,
            redactor,
            ruleEngine,
            PassThroughClassifier()
        )
        return pipeline to scope
    }
@Test
    fun burstOf100_persistsInOneTransactionBatch() = runTest {
        val dao = DedupFakeDao()
        val (pipeline, pipelineScope) = pipeline(dao)
        for (i in 0 until NotificationPipeline.MAX_BATCH_SIZE) {
            pipeline.enqueue(rawNotification(key = "key-$i", postTime = i.toLong(), title = "Title $i", text = "Text $i"))
        }
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(dao.stored).hasSize(100)
        assertThat(dao.insertBatchSizes).containsExactly(100).inOrder()
        assertThat(dao.stored[0].postTime).isEqualTo(0L)
    }

    @Test
    fun burstOf150_splitsIntoBatchesOf100ThenRemainder_afterDrainTimeout() = runTest {
        val dao = DedupFakeDao()
        // StandardTestDispatcher so the consumer does NOT run eagerly during
        // enqueue; items accumulate in the bounded channel first.
        val (pipeline, pipelineScope) = pipeline(dao, useStandardDispatcher = true)
        // Explicitly start the consumer before enqueueing so it's suspended on
        // receiveCatching() when items arrive.
        pipeline.start()
        pipelineScope.testScheduler.advanceUntilIdle() // let consumer reach receive()
        for (i in 0 until 150) {
            pipeline.enqueue(rawNotification(key = "key-$i", postTime = i.toLong(), title = "Title $i", text = "Text $i"))
        }
        // runCurrent() runs only tasks that are immediately scheduleable without
        // advancing virtual time, so the 250 ms drain timeout does NOT fire yet.
        // The consumer wakes up, drains exactly MAX_BATCH_SIZE=100 items (the
        // while-loop exit condition), and suspends waiting for the timeout.
        pipelineScope.testScheduler.runCurrent()
        assertThat(dao.insertBatchSizes).containsExactly(100).inOrder()
        // Advance past the 250 ms drain window: the remaining 50 are flushed.
        pipelineScope.testScheduler.advanceTimeBy(NotificationPipeline.DRAIN_TIMEOUT_MS + 50)
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(dao.insertBatchSizes).containsExactly(100, 50).inOrder()
        assertThat(dao.stored).hasSize(150)
    }

    @Test
    fun reconnectBackfill_doesNotDuplicateHistoryRows_dueToStableHash() = runTest {
        val dao = DedupFakeDao()
        val (pipeline, pipelineScope) = pipeline(dao)
        // Same logical notification posted once, then re-enqueued on listener
        // reconnect (the listener backfills getActiveNotifications()).
        pipeline.enqueue(rawNotification(key = "k1", postTime = 5000L, title = "Same title", text = "Same text"))
        pipeline.enqueue(rawNotification(key = "k1", postTime = 5000L, title = "Same title", text = "Same text"))
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(dao.stored).hasSize(1)
        assertThat(dao.stored.single().key).isEqualTo("k1")
    }

    @Test
    fun sameKeyAndTime_differentContent_createsSeparateRows() = runTest {
        val dao = DedupFakeDao()
        val (pipeline, pipelineScope) = pipeline(dao)
        // Same key + postTime, but redacted content differs -> different textHash.
        pipeline.enqueue(rawNotification(key = "k1", postTime = 5000L, title = "Title", text = "Body A"))
        pipeline.enqueue(rawNotification(key = "k1", postTime = 5000L, title = "Title", text = "Body B"))
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(dao.stored).hasSize(2)
    }

    @Test
    fun muteRule_cancelsByKeyThroughListenerBridge_andStoresMutedStatus() = runTest {
        val dao = DedupFakeDao()
        val bridge = RecordingListenerBridge()
        val (pipeline, pipelineScope) = pipeline(dao, bridge = bridge, ruleEngine = MuteKeyRuleEngine("sale-key"))
        pipeline.enqueue(rawNotification(key = "sale-key", title = "Sale", text = "50% off everything"))
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(bridge.cancelledKeys).containsExactly("sale-key").inOrder()
        assertThat(dao.stored).hasSize(1)
        assertThat(dao.stored[0].status).isEqualTo(NotificationStatus.MUTED)
    }

    @Test
    fun overflow_dropsOldest_keepsBufferBounded() = runTest {
        val dao = DedupFakeDao()
        // StandardTestDispatcher: consumer stays suspended during enqueueing so
        // DROP_OLDEST overflow actually fires when the channel fills to capacity.
        val (pipeline, pipelineScope) = pipeline(dao, useStandardDispatcher = true)
        pipeline.start()
        pipelineScope.testScheduler.advanceUntilIdle() // consumer reaches receive()
        val overflow = 38
        for (i in 0 until (NotificationPipeline.CHANNEL_CAPACITY + overflow)) {
            pipeline.enqueue(rawNotification(key = "key-$i", postTime = i.toLong(), title = "Title $i", text = "Text $i"))
        }
        // runCurrent() + advanceTimeBy loop: flush all batches until the consumer
        // has drained the channel. Each batch is at most MAX_BATCH_SIZE=100 items,
        // so 6 passes cover 512 / 100 = 5.12 → 6 batches.
        repeat(7) {
            pipelineScope.testScheduler.runCurrent()
            pipelineScope.testScheduler.advanceTimeBy(NotificationPipeline.DRAIN_TIMEOUT_MS + 50)
        }
        pipelineScope.testScheduler.advanceUntilIdle()
        val keys = dao.stored.map { it.key }
        assertThat(dao.stored).hasSize(NotificationPipeline.CHANNEL_CAPACITY)
        assertThat(keys).contains("key-${NotificationPipeline.CHANNEL_CAPACITY - 1}")
        assertThat(keys).doesNotContain("key-0") // old entries dropped first
        assertThat(dao.insertBatchSizes.sum()).isEqualTo(NotificationPipeline.CHANNEL_CAPACITY)
    }

    @Test
    fun processingError_skipsOnlyFailingItem_pipelineKeepsWorking() = runTest {
        val dao = DedupFakeDao()
        val (pipeline, pipelineScope) = pipeline(dao, redactor = FailingRedactor("bad-key"))
        pipeline.enqueue(rawNotification(key = "bad-key", title = "Bad", text = "Fails"))
        pipeline.enqueue(rawNotification(key = "good-key", title = "Good", text = "Works"))
        // Advance past the drain window so the batch containing both items is processed.
        pipelineScope.testScheduler.advanceTimeBy(NotificationPipeline.DRAIN_TIMEOUT_MS + 50)
        pipelineScope.testScheduler.advanceUntilIdle()
        assertThat(dao.stored).hasSize(1)
        assertThat(dao.stored[0].key).isEqualTo("good-key")
        assertThat(dao.stored[0].status).isEqualTo(NotificationStatus.KEPT)
    }
}