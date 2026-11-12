package com.invictus.hushbro.notify

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.invictus.hushbro.data.prefs.UserSettings

/** Posts the live countdown and, when explicitly opted in, controls system DND. */
object QuietHoursManager {
    private const val NOTIFICATION_ID = 3001
    private const val DND_PREFS = "hushbro_quiet_dnd_state"
    private const val KEY_MANAGED = "managed_by_hushbro"
    private const val KEY_CHANGED = "changed_by_hushbro"
    private const val KEY_PREVIOUS_FILTER = "previous_filter"

    fun sync(context: Context, settings: UserSettings, nowMillis: Long = System.currentTimeMillis()) {
        val appContext = context.applicationContext
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val endAt = QuietHoursTime.endTimeMillis(settings, nowMillis)
        if (!settings.quietHoursEnabled || endAt == null) {
            notificationManager.cancel(NOTIFICATION_ID)
            restoreSystemDnd(appContext, notificationManager)
            return
        }

        if (settings.quietHoursSystemDndEnabled) applySystemDnd(appContext, notificationManager)
        else restoreSystemDnd(appContext, notificationManager)

        val remaining = (endAt - nowMillis).coerceAtLeast(1_000L)
        val endTime = QuietHoursTime.formatClockTime(settings.quietHoursEnd)
        val dndText = if (settings.quietHoursSystemDndEnabled && notificationManager.isNotificationPolicyAccessGranted) {
            "System Do Not Disturb enabled"
        } else {
            "HushBro is holding notifications"
        }
        val notification = NotificationCompat.Builder(appContext, Channels.CHANNEL_ID_QUIET_HOURS)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .setContentTitle("Quiet hours active")
            .setContentText("$dndText · ends at $endTime")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$dndText. Scheduled end: $endTime. The countdown updates automatically."))
            .setWhen(endAt)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            .setTimeoutAfter(remaining)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        runCatching { notificationManager.notify(NOTIFICATION_ID, notification) }
    }

    private fun applySystemDnd(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !manager.isNotificationPolicyAccessGranted) return
        val prefs = context.getSharedPreferences(DND_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MANAGED, false)) return

        val current = runCatching { manager.currentInterruptionFilter }
            .getOrDefault(NotificationManager.INTERRUPTION_FILTER_ALL)
        val willChange = current == NotificationManager.INTERRUPTION_FILTER_ALL
        prefs.edit()
            .putBoolean(KEY_MANAGED, true)
            .putBoolean(KEY_CHANGED, willChange)
            .putInt(KEY_PREVIOUS_FILTER, current)
            .apply()

        if (willChange) {
            val success = runCatching {
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }.isSuccess
            if (!success) prefs.edit().clear().apply()
        }
    }

    private fun restoreSystemDnd(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val prefs = context.getSharedPreferences(DND_PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_MANAGED, false)) return
        val changedByUs = prefs.getBoolean(KEY_CHANGED, false)
        val previousFilter = prefs.getInt(KEY_PREVIOUS_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
        if (changedByUs && manager.isNotificationPolicyAccessGranted) {
            // Do not overwrite a DND change the user made manually during quiet hours.
            val current = runCatching { manager.currentInterruptionFilter }.getOrNull()
            if (current == NotificationManager.INTERRUPTION_FILTER_NONE) {
                runCatching { manager.setInterruptionFilter(previousFilter) }
            }
        }
        prefs.edit().clear().apply()
    }
}
