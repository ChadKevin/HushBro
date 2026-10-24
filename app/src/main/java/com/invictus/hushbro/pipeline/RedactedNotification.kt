package com.invictus.hushbro.pipeline

/**
 * Notification content after privacy redaction has been applied.
 *
 * Produced by [Redactor] from a [RawNotification]. Redacted title/text is the
 * only content that may be persisted, rule-matched, hashed or logged.
 */
data class RedactedNotification(
    val title: String?,
    val text: String?
)