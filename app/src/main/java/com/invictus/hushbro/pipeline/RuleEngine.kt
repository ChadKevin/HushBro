package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.entity.RuleEntity
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.repo.RuleRepository

interface RuleEngine {
    fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction?
}

class DefaultRuleEngine(
    private val rules: List<RuleEntity> = emptyList(),
    private val ruleRepository: RuleRepository? = null
) : RuleEngine {

    constructor(ruleRepository: RuleRepository) : this(emptyList(), ruleRepository)

    override fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction? {
        val effectiveRules = if (ruleRepository != null) {
            try {
                kotlinx.coroutines.runBlocking { ruleRepository.getEnabledRulesSync() }
            } catch (e: Throwable) {
                rules
            }
        } else {
            rules
        }
        for (rule in effectiveRules.sortedBy { it.priority }) {
            if (!rule.enabled) continue
            if (rule.appPackage != null && rule.appPackage != raw.packageName) continue
            val haystack = (redacted.title.orEmpty() + " " + redacted.text.orEmpty()).lowercase()
            val keyword = rule.keyword.trim()
            if (keyword.isNotEmpty() && !haystack.contains(keyword.lowercase())) continue
            return rule.action
        }
        return null
    }
}

class PassThroughRuleEngine : RuleEngine {
    override fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction? = null
}
