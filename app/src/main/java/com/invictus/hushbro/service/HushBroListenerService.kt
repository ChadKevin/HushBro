package com.invictus.hushbro.service

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.invictus.hushbro.pipeline.NotificationPipeline
import com.invictus.hushbro.pipeline.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HushBroListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HushBroListener"
    }

    @Inject
    lateinit var listenerBridge: ListenerBridge

    @Inject
    lateinit var pipeline: NotificationPipeline

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        listenerBridge.attachService(this)
        backfillActiveNotifications()
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
        val raw = toRawNotification(sbn)
        if (raw != null) {
            pipeline.enqueue(raw)
            // Safe metadata only; never raw notification content per privacy rules.
            Log.d(TAG, "Notification enqueued for buffered processing: pkg=${raw.packageName}, postTime=${raw.postTime}")
        }
        listenerBridge.updateActiveCount()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        Log.d(TAG, "Notification removed: pkg=${sbn.packageName}, postTime=${sbn.postTime}")
        listenerBridge.updateActiveCount()
    }

    /**
     * On every listener (re)connect the currently active notifications are
     * enqueued so history is back-filled. The pipeline computes a stable
     * (key, postTime, textHash) identity and the database's unique index drops
     * rows that are already stored, so reconnecting never duplicates history.
     */
    private fun backfillActiveNotifications() {
        var enqueued = 0
        for (sbn in listenerBridge.getActiveNotifications()) {
            val raw = toRawNotification(sbn)
            if (raw != null && pipeline.enqueue(raw)) {
                enqueued++
            }
        }
        if (enqueued > 0) {
            Log.d(TAG, "Back-filled $enqueued active notification(s); duplicates are dropped by the unique index")
        }
    }

    /**
     * Extracts the minimal fields a notification is reduced to before buffering.
     * This is the ONLY work the listener callback performs: no Room queries, no
     * regex, no rule evaluation, no file I/O and no suspending calls.
     */
    private fun toRawNotification(sbn: StatusBarNotification?): RawNotification? {
        if (sbn == null) return null
        val pkg = sbn.packageName ?: return null

        // Ignore HushBro's own notifications (digests, reminders)
        if (pkg == packageName) return null

        // Ignore ongoing notifications (media players, foreground services)
        if (sbn.isOngoing) return null

        val notification = sbn.notification ?: return null

        // Ignore group summaries
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return null

        // Safe app label resolution with package name fallback
        val appLabel = runCatching {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(pkg)

        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        return RawNotification(
            key = sbn.key,
            packageName = pkg,
            appLabel = appLabel,
            title = title,
            text = text,
            postTime = sbn.postTime,
            category = notification.category,
            visibility = notification.visibility
        )
    }
}
