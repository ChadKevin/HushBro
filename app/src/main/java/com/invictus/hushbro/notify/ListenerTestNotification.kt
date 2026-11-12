package com.invictus.hushbro.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * A self-posted test notification used to verify notification listener access.
 *
 * The listener service filters these out of normal processing via
 * [HushBroListenerService.isListenerTestNotification], and [ListenerBridge]
 * tracks the pass/fail state of the test.
 */
object ListenerTestNotification {
    const val TAG = "HushBroListenerTest"
    const val ID = 1001

    fun post(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TAG,
                "HushBro listener test",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Verifies notification listener access"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(context, TAG)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HushBro")
            .setContentText("Listener access test")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(TAG, ID, builder.build())

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
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.flags = notification.flags or Notification.FLAG_ONLY_ALERT_ONCE
        }

        manager.notify(TAG, ID, notification)
    }
}
