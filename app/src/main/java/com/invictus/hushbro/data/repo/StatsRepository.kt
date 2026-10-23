package com.invictus.hushbro.data.repo

import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.model.AppSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    fun getAppSummaries(): Flow<List<AppSummary>> =
        notificationDao.getAppSummaries()

    suspend fun getTotalNotificationsCount(): Int =
        notificationDao.getCount()
}
