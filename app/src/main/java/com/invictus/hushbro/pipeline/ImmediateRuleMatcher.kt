package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.entity.RuleEntity
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evaluates rules from an in-memory snapshot on the listener callback path.
 * Rule changes are observed asynchronously; this class never queries Room from
 * onNotificationPosted(), keeping callback processing short.
 */
@Singleton
class ImmediateRuleMatcher @Inject constructor(
    @ApplicationScope appScope: CoroutineScope,
    ruleRepository: RuleRepository,
    private val redactor: Redactor
) {
    @Volatile
    private var enabledRules: List<RuleEntity> = emptyList()

    @Volatile
    private var rulesLoaded: Boolean = false

    init {
        appScope.launch {
            ruleRepository.getEnabledRules().collect { rules ->
                enabledRules = rules.sortedBy { it.priority }
                rulesLoaded = true
            }
        }
    }

    fun isReady(): Boolean = rulesLoaded

    fun evaluate(raw: RawNotification): RuleAction? {
        if (!rulesLoaded) return null

        val redacted = redactor.redact(raw)
        val haystack = (redacted.title.orEmpty() + " " + redacted.text.orEmpty())
            .lowercase()

        for (rule in enabledRules) {
            if (!rule.enabled) continue
            if (rule.appPackage != null && rule.appPackage != raw.packageName) continue

            val keyword = rule.keyword.trim()
            if (keyword.isNotEmpty() && !haystack.contains(keyword.lowercase())) continue

            return rule.action
        }

        return null
    }
}
