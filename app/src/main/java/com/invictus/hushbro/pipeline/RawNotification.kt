package com.invictus.hushbro.pipeline

/**
 * Temporary, in-memory-only representation of a captured notification.
 *
 * Raw title/text MUST NOT be persisted or logged before it passes through the
 * [Redactor] privacy pipeline. This object is only ever buffered in the bounded
 * channel of [NotificationPipeline] and processed off the listener callback thread.
 *
 * `category` (Android notification category string) and `visibility`
 * (`android.app.Notification.visibility` value) are carried for the classifier
 * and the redactor steps that consume them; they are never stored in the database
 * (the [NotificationEntity][com.invictus.hushbro.data.db.entity.NotificationEntity]
 * model has no such columns).
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
    val visibility: Int = 0
)