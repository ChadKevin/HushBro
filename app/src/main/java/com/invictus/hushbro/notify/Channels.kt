package com.invictus.hushbro.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Channels {

    const val CHANNEL_ID_DIGEST = "hushbro_digest"
    const val CHANNEL_ID_SNOOZE = "hushbro_snooze"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val digestChannel = NotificationChannel(
                CHANNEL_ID_DIGEST,
                "Notification Digest",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Scheduled summaries and notification bundles from HushBro"
                setShowBadge(true)
            }

            val snoozeChannel = NotificationChannel(
                CHANNEL_ID_SNOOZE,
                "Snoozed Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for snoozed notifications"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(digestChannel, snoozeChannel))
        }
    }
}
