package com.invictus.hushbro.data.repo

import com.invictus.hushbro.data.db.dao.AppPrefDao
import com.invictus.hushbro.data.db.entity.AppPrefEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefRepository @Inject constructor(
    private val appPrefDao: AppPrefDao
) {

    fun observeAll(): Flow<List<AppPrefEntity>> =
        appPrefDao.observeAll()

    fun observePreference(packageName: String): Flow<AppPrefEntity?> =
        appPrefDao.getPreference(packageName)

    suspend fun getPreference(packageName: String): AppPrefEntity? =
        appPrefDao.getPreferenceSync(packageName)

    suspend fun getAllSync(): List<AppPrefEntity> =
        appPrefDao.getAllSync()

    suspend fun upsert(pref: AppPrefEntity) =
        appPrefDao.upsert(pref)

    suspend fun setPrivate(packageName: String, isPrivate: Boolean) {
        val current = appPrefDao.getPreferenceSync(packageName) ?: AppPrefEntity(packageName = packageName)
        appPrefDao.upsert(current.copy(isPrivate = isPrivate))
    }

    suspend fun setDigestEnabled(packageName: String, enabled: Boolean) {
        val current = appPrefDao.getPreferenceSync(packageName) ?: AppPrefEntity(packageName = packageName)
        appPrefDao.upsert(current.copy(digestEnabled = enabled))
    }

    suspend fun delete(packageName: String) =
        appPrefDao.delete(packageName)
}
