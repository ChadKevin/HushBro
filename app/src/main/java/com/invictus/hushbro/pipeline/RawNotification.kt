package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.model.RuleAction

/**
 * Temporary, in-memory-only representation of a captured notification.
 *
 * Raw title/text MUST NOT be persisted or logged before it passes through the
 * [Redactor] privacy pipeline. This object is only ever buffered in the bounded
 * channel of [NotificationPipeline] and processed off the listener callback thread.
 *
 * `category` (Android notification category string) and `visibility`
 * (`android.app.Notification.visibility` value) are carried for the classifier
 * and the redactor steps that consume them; they are never stored in the database.
 */
data class RawNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String?,
    val text: String?,
    val postTime: Long,
    val category: String? = null,
    /** android.app.Notification.visibility; 0 == VISIBILITY_PRIVATE. */
    val visibility: Int = 0,
    /** Action evaluated from the in-memory rule snapshot in the listener callback. */
    val preEvaluatedAction: RuleAction? = null,
    /** True when the listener evaluated the current rule snapshot before enqueueing. */
    val rulesEvaluated: Boolean = false,
    /** True for listener callbacks where cancellation, if needed, was already handled immediately. */
    val listenerCallbackHandled: Boolean = false
)
