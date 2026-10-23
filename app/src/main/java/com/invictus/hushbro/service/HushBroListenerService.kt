package com.invictus.hushbro.service

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HushBroListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HushBroListener"
    }

    @Inject
    lateinit var listenerBridge: ListenerBridge

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        listenerBridge.attachService(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        listenerBridge.detachService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, HushBroListenerService::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return

        // Ignore HushBro's own notifications (digests, reminders)
        if (pkg == packageName) {
            return
        }

        // Ignore ongoing notifications (media players, foreground services)
        if (sbn.isOngoing) {
            return
        }

        val notification = sbn.notification ?: return

        // Ignore group summaries
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        // Safe app label resolution with package name fallback
        val appLabel = runCatching {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(pkg)

        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val postTime = sbn.postTime
        val category = notification.category
        val key = sbn.key

        // Log only safe metadata (never print raw notification content in logs per privacy rules)
        Log.d(
            TAG,
            "Notification observed: pkg=$pkg, label=$appLabel, category=$category, postTime=$postTime, hasTitle=${!title.isNullOrEmpty()}, hasText=${!text.isNullOrEmpty()}"
        )

        listenerBridge.updateActiveCount()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        Log.d(TAG, "Notification removed: pkg=${sbn.packageName}, postTime=${sbn.postTime}")
        listenerBridge.updateActiveCount()
    }
}
