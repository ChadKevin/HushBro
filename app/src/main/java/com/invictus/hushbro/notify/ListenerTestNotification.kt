package com.invictus.hushbro.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * A quiet, uniquely tagged notification used to verify that HushBro's
 * NotificationListenerService receives callbacks. The listener dismisses it
 * after receiving the callback and reports the result to ListenerBridge.
 */
object ListenerTestNotification {
    const val TAG = "hushbro_listener_test"
    const val ID = 41_729
    const val CHANNEL_ID = "hushbro_listener_test"

    fun post(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "HushBro Listener Test",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "A quiet test used to verify notification-listener access."
                        setSound(null, null)
                        enableVibration(false)
                        setShowBadge(false)
                    }
                )
            }
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context).setPriority(Notification.PRIORITY_MIN)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("HushBro listener test")
            .setContentText("Checking whether HushBro can receive notification callbacks.")
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setOngoing(false)
            .setSilent(true)
            .build()

        manager.notify(TAG, ID, notification)
    }
}
