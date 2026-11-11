package com.invictus.hushbro.notify

import com.invictus.hushbro.data.prefs.UserSettings
import java.util.Calendar

/** Calendar/time helpers shared by the settings UI and the background worker. */
object QuietHoursTime {
    fun isActive(settings: UserSettings, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!settings.quietHoursEnabled) return false
        val start = settings.quietHoursStart.coerceIn(0, 1439)
        val end = settings.quietHoursEnd.coerceIn(0, 1439)
        if (start == end) return false
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return if (start < end) minute >= start && minute < end else minute >= start || minute < end
    }

    fun endTimeMillis(settings: UserSettings, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (!isActive(settings, nowMillis)) return null
        val start = settings.quietHoursStart.coerceIn(0, 1439)
        val end = settings.quietHoursEnd.coerceIn(0, 1439)
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val endCalendar = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, end / 60)
            set(Calendar.MINUTE, end % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (start > end && currentMinute >= start) add(Calendar.DAY_OF_YEAR, 1)
        }
        return endCalendar.timeInMillis
    }

    fun nextTransitionMillis(settings: UserSettings, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (!settings.quietHoursEnabled) return null
        val start = settings.quietHoursStart.coerceIn(0, 1439)
        val end = settings.quietHoursEnd.coerceIn(0, 1439)
        if (start == end) return null
        if (isActive(settings, nowMillis)) return endTimeMillis(settings, nowMillis)

        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val nextStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, start / 60)
            set(Calendar.MINUTE, start % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (currentMinute >= start) add(Calendar.DAY_OF_YEAR, 1)
        }
        return nextStart.timeInMillis
    }

    fun formatDuration(millis: Long): String {
        val seconds = (millis.coerceAtLeast(0L) / 1_000L)
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
    }

    fun formatClockTime(hourMinute: Int): String =
        "%02d:%02d".format(hourMinute.coerceIn(0, 1439) / 60, hourMinute.coerceIn(0, 1439) % 60)
}
