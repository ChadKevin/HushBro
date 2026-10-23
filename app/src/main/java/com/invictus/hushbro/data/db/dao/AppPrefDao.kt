package com.invictus.hushbro.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.invictus.hushbro.data.db.entity.AppPrefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPrefDao {

    @Query("SELECT * FROM app_prefs")
    fun observeAll(): Flow<List<AppPrefEntity>>

    @Upsert
    suspend fun upsert(pref: AppPrefEntity)

    @Upsert
    suspend fun upsertAll(prefs: List<AppPrefEntity>)

    @Query("SELECT * FROM app_prefs WHERE packageName = :packageName")
    fun getPreference(packageName: String): Flow<AppPrefEntity?>

    @Query("SELECT * FROM app_prefs WHERE packageName = :packageName")
    suspend fun getPreferenceSync(packageName: String): AppPrefEntity?

    @Query("SELECT * FROM app_prefs")
    suspend fun getAllSync(): List<AppPrefEntity>

    @Query("DELETE FROM app_prefs WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
