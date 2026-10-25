package com.invictus.hushbro.data.repo

import androidx.paging.PagingSource
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.AppSummary
import com.invictus.hushbro.data.db.model.NotificationStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    fun searchPaged(query: String): PagingSource<Int, NotificationEntity> =
        notificationDao.searchPaged(query)

    fun search(query: String): Flow<List<NotificationEntity>> =
        notificationDao.search(query)

    fun getPagedByApp(packageName: String): PagingSource<Int, NotificationEntity> =
        notificationDao.getPagedByApp(packageName)

    fun getByApp(packageName: String): Flow<List<NotificationEntity>> =
        notificationDao.getByApp(packageName)

    fun getAppSummaries(): Flow<List<AppSummary>> =
        notificationDao.getAppSummaries()

    fun getAllNotifications(): Flow<List<NotificationEntity>> =
        notificationDao.getAllFlow()

    suspend fun getById(id: Long): NotificationEntity? =
        notificationDao.getById(id)

    suspend fun insert(notification: NotificationEntity): Long =
        notificationDao.insert(notification)

    suspend fun insertBatch(notifications: List<NotificationEntity>): List<Long> =
        notificationDao.insertBatch(notifications)

    suspend fun markRead(id: Long) =
        notificationDao.markRead(id)

    suspend fun markAllRead() =
        notificationDao.markAllRead()

    suspend fun markAppRead(packageName: String) =
        notificationDao.markAppRead(packageName)

    suspend fun clearAll() =
        notificationDao.clearAll()

    suspend fun clearApp(packageName: String) =
        notificationDao.clearApp(packageName)

    suspend fun updateStatus(id: Long, status: NotificationStatus) =
        notificationDao.updateStatus(id, status)

    suspend fun updateStatusBatch(ids: List<Long>, status: NotificationStatus) =
        notificationDao.updateStatusBatch(ids, status)

    suspend fun getHeldForDigest(upToTime: Long): List<NotificationEntity> =
        notificationDao.getHeldForDigest(upToTime)

    suspend fun deleteById(id: Long) =
        notificationDao.deleteById(id)

    suspend fun deleteOlderThan(timestamp: Long): Int =
        notificationDao.deleteOlderThan(timestamp)

    suspend fun getCount(): Int =
        notificationDao.getCount()
}
