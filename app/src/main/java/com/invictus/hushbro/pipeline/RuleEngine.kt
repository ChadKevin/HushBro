package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.model.RuleAction

/**
 * Evaluates the ordered, enabled rules against a notification and returns the
 * first matching action, or `null` when no rule matches.
 *
 * Rules must only match against redacted title/text so that rules and metadata
 * cannot accidentally persist sensitive content. The complete rule engine lands
 * in a later step; [PassThroughRuleEngine] currently matches nothing.
 */
interface RuleEngine {

    fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction?
}

/** Temporary no-op implementation used until the rule-engine step. */
class PassThroughRuleEngine : RuleEngine {

    override fun evaluate(raw: RawNotification, redacted: RedactedNotification): RuleAction? = null
}