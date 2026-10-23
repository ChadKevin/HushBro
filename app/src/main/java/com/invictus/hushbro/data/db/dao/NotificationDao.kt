package com.invictus.hushbro.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.AppSummary
import com.invictus.hushbro.data.db.model.NotificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatch(notifications: List<NotificationEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query(
        """
        SELECT * FROM notifications 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appLabel LIKE '%' || :query || '%')
        ORDER BY postTime DESC
        """
    )
    fun searchPaged(query: String): PagingSource<Int, NotificationEntity>

    @Query(
        """
        SELECT * FROM notifications 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appLabel LIKE '%' || :query || '%')
        ORDER BY postTime DESC
        """
    )
    fun search(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY postTime DESC")
    fun getPagedByApp(packageName: String): PagingSource<Int, NotificationEntity>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY postTime DESC")
    fun getByApp(packageName: String): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT packageName, appLabel, 
               COUNT(*) AS totalCount, 
               SUM(CASE WHEN isRead = 0 THEN 1 ELSE 0 END) AS unreadCount, 
               MAX(postTime) AS latestPostTime 
        FROM notifications 
        GROUP BY packageName, appLabel 
        ORDER BY latestPostTime DESC
        """
    )
    fun getAppSummaries(): Flow<List<AppSummary>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllRead()

    @Query("UPDATE notifications SET isRead = 1 WHERE packageName = :packageName AND isRead = 0")
    suspend fun markAppRead(packageName: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    @Query("DELETE FROM notifications WHERE packageName = :packageName")
    suspend fun clearApp(packageName: String)

    @Query("UPDATE notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: NotificationStatus)

    @Query("UPDATE notifications SET status = :status WHERE id IN (:ids)")
    suspend fun updateStatusBatch(ids: List<Long>, status: NotificationStatus)

    @Query("SELECT * FROM notifications WHERE status = 'HELD' AND postTime <= :upToTime ORDER BY postTime ASC")
    suspend fun getHeldForDigest(upToTime: Long): List<NotificationEntity>

    @Query("DELETE FROM notifications WHERE postTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getById(id: Long): NotificationEntity?

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getCount(): Int

    @Query("SELECT * FROM notifications ORDER BY postTime DESC")
    fun getAllFlow(): Flow<List<NotificationEntity>>
}
