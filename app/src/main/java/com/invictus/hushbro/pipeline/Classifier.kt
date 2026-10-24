package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.model.NotificationTag

/**
 * On-device classifier that assigns one of the local tags (OTP, PROMO, SOCIAL,
 * OTHER) to a captured notification.
 *
 * The classifier may inspect raw text transiently for OTP tagging, but raw text
 * must never be stored or logged. The full classifier lands in a later step;
 * [PassThroughClassifier] currently tags everything as OTHER.
 */
interface Classifier {

    fun classify(raw: RawNotification, redacted: RedactedNotification): NotificationTag
}

/** Temporary no-op implementation used until the classifier step. */
class PassThroughClassifier : Classifier {

    override fun classify(raw: RawNotification, redacted: RedactedNotification): NotificationTag =
        NotificationTag.OTHER
}