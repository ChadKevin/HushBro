package com.invictus.hushbro.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.invictus.hushbro.data.db.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY priority ASC")
    fun observeRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY priority ASC")
    fun getEnabledRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledRulesSync(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE id = :id")
    suspend fun getById(id: Long): RuleEntity?

    @Upsert
    suspend fun upsert(rule: RuleEntity): Long

    @Upsert
    suspend fun upsertAll(rules: List<RuleEntity>)

    @Delete
    suspend fun delete(rule: RuleEntity)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE rules SET priority = :priority WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: Int)

    @Transaction
    suspend fun swapPriority(id1: Long, priority1: Int, id2: Long, priority2: Int) {
        updatePriority(id1, priority2)
        updatePriority(id2, priority1)
    }

    @Query("SELECT COUNT(*) FROM rules")
    suspend fun getCount(): Int
}
