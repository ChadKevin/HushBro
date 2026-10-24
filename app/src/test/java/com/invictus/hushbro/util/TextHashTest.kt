package com.invictus.hushbro.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextHashTest {

    @Test
    fun isStableForSameRedactedContent_onEveryAttempt() {
        assertThat(TextHash.of("Your OTP is ****", "Enter the code on sign-in"))
            .isEqualTo(TextHash.of("Your OTP is ****", "Enter the code on sign-in"))
    }

    @Test
    fun differsWhenContentDiffers() {
        assertThat(TextHash.of("Title A", "Body A")).isNotEqualTo(TextHash.of("Title A", "Body B"))
        assertThat(TextHash.of("Title A", "Body A")).isNotEqualTo(TextHash.of("Title B", "Body A"))
    }

    @Test
    fun distinguishesFieldBoundaries() {
        // ("ab", "c") and ("a", "bc") share the same characters but are different fields.
        assertThat(TextHash.of("ab", "c")).isNotEqualTo(TextHash.of("a", "bc"))
    }

    @Test
    fun nullAndEmptyAreStableAndDistinct() {
        assertThat(TextHash.of(null, null)).isEqualTo(TextHash.of(null, null))
        assertThat(TextHash.of("", "")).isEqualTo(TextHash.of("", ""))
        assertThat(TextHash.of(null, "text")).isEqualTo(TextHash.of(null, "text"))
        assertThat(TextHash.of(null, null)).isNotEqualTo(TextHash.of("", ""))
    }

    @Test
    fun producesStableFixedWidthHexHash() {
        assertThat(TextHash.of("one", "two")).matches("[0-9a-fA-F]{64}")
    }
}