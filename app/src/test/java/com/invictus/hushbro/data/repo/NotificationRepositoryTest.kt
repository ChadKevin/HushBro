package com.invictus.hushbro.data.repo

import com.google.common.truth.Truth.assertThat
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.AppSummary
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import androidx.paging.PagingSource

class NotificationRepositoryTest {

    private class FakeNotificationDao : NotificationDao {
        val stored = mutableListOf<NotificationEntity>()

        override suspend fun insertBatch(notifications: List<NotificationEntity>): List<Long> {
            val ids = mutableListOf<Long>()
            for (n in notifications) {
                stored.add(n)
                ids.add(stored.size.toLong())
            }
            return ids
        }

        override suspend fun insert(notification: NotificationEntity): Long {
            stored.add(notification)
            return stored.size.toLong()
        }

        override fun searchPaged(query: String): PagingSource<Int, NotificationEntity> {
            throw NotImplementedError()
        }

        override fun search(query: String) = flowOf(stored.filter { it.title?.contains(query) == true })

        override fun getPagedByApp(packageName: String): PagingSource<Int, NotificationEntity> {
            throw NotImplementedError()
        }

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

        override suspend fun deleteOlderThan(timestamp: Long): Int = 0

        override suspend fun getById(id: Long): NotificationEntity? = null

        override suspend fun getCount(): Int = stored.size

        override fun getAllFlow() = flowOf(stored.toList())
    }

    private lateinit var fakeDao: FakeNotificationDao
    private lateinit var repository: NotificationRepository

    @Before
    fun setup() {
        fakeDao = FakeNotificationDao()
        repository = NotificationRepository(fakeDao)
    }

    @Test
    fun repository_insertAndCount() = runTest {
        val item = NotificationEntity(
            key = "k1",
            packageName = "pkg",
            appLabel = "App",
            title = "Title",
            text = "Text",
            postTime = 100L,
            tag = NotificationTag.SOCIAL,
            status = NotificationStatus.KEPT,
            textHash = "hash"
        )
        val id = repository.insert(item)
        assertThat(id).isEqualTo(1L)
        assertThat(repository.getCount()).isEqualTo(1)

        repository.clearAll()
        assertThat(repository.getCount()).isEqualTo(0)
    }
}
