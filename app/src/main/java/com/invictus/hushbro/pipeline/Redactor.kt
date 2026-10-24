package com.invictus.hushbro.pipeline

/** Temporary no-op implementation retained for tests. */
class PassThroughRedactor : Redactor {
    override fun redact(raw: RawNotification): RedactedNotification =
        RedactedNotification(title = raw.title, text = raw.text)
}

/**
 * Privacy pipeline: turns raw captured content into the redacted content that is
 * safe to persist, rule-match, hash and log.
 *
 * The full implementation (OTP/card masking, private-app and secret-visibility
 * handling) lands in a later step. Until then [PassThroughRedactor] keeps the
 * content unchanged so the buffered pipeline and batch writer can be completed
 * and verified independently.
 */
interface Redactor {

    fun redact(raw: RawNotification): RedactedNotification
}

class DefaultRedactor : Redactor {

    override fun redact(raw: RawNotification): RedactedNotification {
        if (raw.visibility == android.app.Notification.VISIBILITY_SECRET) {
            return RedactedNotification(title = null, text = null)
        }
        return RedactedNotification(
            title = redactText(raw.title),
            text = redactText(raw.text)
        )
    }

    private fun redactText(input: String?): String? {
        if (input == null) return null
        var out = input
        out = OTP_REGEX.replace(out, "[OTP]")
        out = CARD_REGEX.replace(out, "[CARD]")
        out = EMAIL_REGEX.replace(out, "[EMAIL]")
        out = PHONE_REGEX.replace(out, "[PHONE]")
        return out
    }

    companion object {
        private val OTP_REGEX = Regex("\\b\\d{4,8}\\b")
        private val CARD_REGEX = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
        private val EMAIL_REGEX = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        private val PHONE_REGEX = Regex("\\+?\\d[\\d\\s().-]{7,}\\d")
    }
}