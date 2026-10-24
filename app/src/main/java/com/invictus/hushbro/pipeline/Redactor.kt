package com.invictus.hushbro.pipeline

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

/** Temporary no-op implementation used until the redaction step. */
class PassThroughRedactor : Redactor {

    override fun redact(raw: RawNotification): RedactedNotification =
        RedactedNotification(
            title = raw.title,
            text = raw.text
        )
}