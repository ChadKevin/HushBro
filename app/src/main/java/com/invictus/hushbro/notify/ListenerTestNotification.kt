package com.invictus.hushbro.notify

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
    }
}
