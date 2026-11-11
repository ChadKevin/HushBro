package com.invictus.hushbro.notify

import com.invictus.hushbro.data.prefs.UserSettings
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class QuietHoursTimeTest {
    private fun time(hour: Int, minute: Int = 0): Long = Calendar.getInstance().apply {
        set(2026, Calendar.MARCH, 10, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun overnightWindowIsActiveAfterMidnightAndEndsSameMorning() {
        val settings = UserSettings(quietHoursEnabled = true, quietHoursStart = 22 * 60, quietHoursEnd = 7 * 60)
        val now = time(1, 30)
        assertThat(QuietHoursTime.isActive(settings, now)).isTrue()
        val expected = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertThat(QuietHoursTime.endTimeMillis(settings, now)).isEqualTo(expected)
    }

    @Test
    fun inactiveOvernightWindowSchedulesStartLaterToday() {
        val settings = UserSettings(quietHoursEnabled = true, quietHoursStart = 22 * 60, quietHoursEnd = 7 * 60)
        val now = time(10, 0)
        assertThat(QuietHoursTime.isActive(settings, now)).isFalse()
        val expected = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertThat(QuietHoursTime.nextTransitionMillis(settings, now)).isEqualTo(expected)
    }

    @Test
    fun equalStartAndEndDisablesWindow() {
        val settings = UserSettings(quietHoursEnabled = true, quietHoursStart = 8 * 60, quietHoursEnd = 8 * 60)
        assertThat(QuietHoursTime.isActive(settings, time(8, 30))).isFalse()
        assertThat(QuietHoursTime.nextTransitionMillis(settings, time(8, 30))).isNull()
    }
}
