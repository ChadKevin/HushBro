package com.invictus.hushbro.pipeline

import com.invictus.hushbro.data.db.model.NotificationTag

interface Classifier {
    fun classify(raw: RawNotification, redacted: RedactedNotification): NotificationTag
}

class DefaultClassifier : Classifier {
    override fun classify(raw: RawNotification, redacted: RedactedNotification): NotificationTag {
        val text = (redacted.title.orEmpty() + " " + redacted.text.orEmpty()).lowercase()
        if (OTP_WORDS.any { text.contains(it) } || OTP_REGEX.containsMatchIn(text)) return NotificationTag.OTP
        if (PROMO_WORDS.any { text.contains(it) }) return NotificationTag.PROMO
        if (SOCIAL_WORDS.any { text.contains(it) } || raw.category == "social") return NotificationTag.SOCIAL
        return NotificationTag.OTHER
    }

    companion object {
        private val OTP_WORDS = listOf("otp", "verification code", "one-time", "passcode")
        private val OTP_REGEX = Regex("\\b\\d{4,8}\\b")
        private val PROMO_WORDS = listOf("sale", "discount", "offer", "promo", "deal", "cashback")
        private val SOCIAL_WORDS = listOf("message", "chat", "friend", "liked", "comment", "reply", "tagged")
    }
}

class PassThroughClassifier : Classifier {
    override fun classify(raw: RawNotification, redacted: RedactedNotification): NotificationTag =
        NotificationTag.OTHER
}
