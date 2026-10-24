package com.invictus.hushbro.util

import java.io.ByteArrayOutputStream
import java.security.MessageDigest

/**
 * Creates the stable notification content hash used by the deduplication index.
 *
 * The hash is derived on every processing attempt from the SAME redacted
 * title/text fields passed through the privacy pipeline, so identical content
 * always produces the identical hash. Together with the unique
 * `(key, postTime, textHash)` index on the notifications table this prevents
 * duplicate history rows when the listener reconnects and re-enqueues active
 * notifications.
 *
 * Encoding is a deterministic, length-prefixed stream of UTF-16 code units, so
 * the result is identical on Android and the host JVM for any given content.
 */
object TextHash {

    fun of(title: String?, text: String?): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = ByteArrayOutputStream()
        writeNullableString(input, title)
        writeNullableString(input, text)
        digest.update(input.toByteArray())
        return digest.digest()
            .map { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') }
            .joinToString("")
    }

    private fun writeNullableString(out: ByteArrayOutputStream, value: String?) {
        if (value == null) {
            writeInt(out, -1)
            return
        }
        writeInt(out, value.length)
        for (i in 0 until value.length) {
            writeInt(out, value[i].code.toInt())
        }
    }

    private fun writeInt(out: ByteArrayOutputStream, value: Int) {
        out.write((value ushr 24) and 0xFF)
        out.write((value ushr 16) and 0xFF)
        out.write((value ushr 8) and 0xFF)
        out.write(value and 0xFF)
    }
}