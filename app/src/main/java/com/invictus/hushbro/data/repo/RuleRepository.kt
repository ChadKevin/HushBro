package com.invictus.hushbro.data.repo

import com.invictus.hushbro.data.db.dao.RuleDao
import com.invictus.hushbro.data.db.entity.RuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao
) {

    fun observeRules(): Flow<List<RuleEntity>> =
        ruleDao.observeRules()

    fun getEnabledRules(): Flow<List<RuleEntity>> =
        ruleDao.getEnabledRules()

    suspend fun getEnabledRulesSync(): List<RuleEntity> =
        ruleDao.getEnabledRulesSync()

    suspend fun getById(id: Long): RuleEntity? =
        ruleDao.getById(id)

    suspend fun upsert(rule: RuleEntity): Long =
        ruleDao.upsert(rule)

    suspend fun upsertAll(rules: List<RuleEntity>) =
        ruleDao.upsertAll(rules)

    suspend fun delete(rule: RuleEntity) =
        ruleDao.delete(rule)

    suspend fun deleteById(id: Long) =
        ruleDao.deleteById(id)

    suspend fun swapPriority(id1: Long, priority1: Int, id2: Long, priority2: Int) =
        ruleDao.swapPriority(id1, priority1, id2, priority2)

    suspend fun getCount(): Int =
        ruleDao.getCount()
}
