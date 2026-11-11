package com.invictus.hushbro.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Channels {
    const val CHANNEL_ID_DIGEST = "hushbro_digest"
    const val CHANNEL_ID_SNOOZE = "hushbro_snooze"
    const val CHANNEL_ID_QUIET_HOURS = "hushbro_quiet_hours"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val digest = NotificationChannel(
            CHANNEL_ID_DIGEST,
            "Notification Digest",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Scheduled summaries and notification bundles from HushBro"
            setShowBadge(true)
        }
        val snooze = NotificationChannel(
            CHANNEL_ID_SNOOZE,
            "Snoozed Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for snoozed notifications"
            setShowBadge(true)
        }
        val quiet = NotificationChannel(
            CHANNEL_ID_QUIET_HOURS,
            "Quiet Hours Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "An ongoing countdown while HushBro quiet hours are active"
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannels(listOf(digest, snooze, quiet))
    }
}
