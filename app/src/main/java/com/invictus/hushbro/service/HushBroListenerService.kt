package com.invictus.hushbro.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.notify.ListenerTestNotification
import com.invictus.hushbro.pipeline.ImmediateRuleMatcher
import com.invictus.hushbro.pipeline.NotificationPipeline
import com.invictus.hushbro.pipeline.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HushBroListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HushBroListener"
    }

    @Inject lateinit var listenerBridge: ListenerBridge
    @Inject lateinit var pipeline: NotificationPipeline
    @Inject lateinit var immediateRuleMatcher: ImmediateRuleMatcher

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        listenerBridge.attachService(this)
        runCatching { backfillActiveNotifications() }
            .onFailure { Log.w(TAG, "Back-fill of active notifications failed", it) }
        listenerBridge.updateActiveCount()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        listenerBridge.detachService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching {
                requestRebind(ComponentName(this, HushBroListenerService::class.java))
            }.onFailure {
                Log.w(TAG, "Could not request notification-listener rebind", it)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        processNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        Log.d(TAG, "Notification removed: pkg=${sbn.packageName}, postTime=${sbn.postTime}")
        listenerBridge.updateActiveCount()
    }

    /**
     * Evaluate a cached rule snapshot on the listener callback path. When a
     * rule matches MUTE, request cancellation for this exact Android notification
     * key before sending the record to the asynchronous persistence pipeline.
     */
    private fun processNotification(sbn: StatusBarNotification?) {
        if (sbn == null) return

        if (isListenerTestNotification(sbn)) {
            runCatching { cancelNotification(sbn.key) }
                .onFailure { Log.w(TAG, "Could not dismiss listener test notification", it) }
            listenerBridge.markListenerTestPassed()
            listenerBridge.updateActiveCount()
            return
        }

        val raw = toRawNotification(sbn) ?: return
        val rulesReady = immediateRuleMatcher.isReady()
        val action = if (rulesReady) immediateRuleMatcher.evaluate(raw) else null

        if (action == RuleAction.MUTE) {
            // Cancel only this notification key, never every notification from the app.
            runCatching { cancelNotification(sbn.key) }
                .onFailure {
                    Log.w(TAG, "Could not cancel matching notification key (pkg=${raw.packageName})", it)
                }
        }

        // Persist asynchronously. Never issue a delayed cancellation for this
        // callback: an app may reuse the same key for a later message.
        val queued = raw.copy(
            preEvaluatedAction = action,
            rulesEvaluated = rulesReady,
            listenerCallbackHandled = true
        )
        if (!pipeline.enqueue(queued)) {
            Log.w(TAG, "Notification pipeline rejected an event (pkg=${raw.packageName}, postTime=${raw.postTime})")
        }

        listenerBridge.updateActiveCount()
    }

    private fun isListenerTestNotification(sbn: StatusBarNotification): Boolean =
        sbn.packageName == packageName &&
            sbn.tag == ListenerTestNotification.TAG &&
            sbn.id == ListenerTestNotification.ID

    /**
     * On every listener (re)connect, process currently active notifications as
     * well. Muted notifications are cancelled by their exact keys; saved history
     * is deduplicated by the database's unique index.
     */
    private fun backfillActiveNotifications() {
        var processed = 0
        for (sbn in listenerBridge.getActiveNotifications()) {
            if (sbn.packageName == packageName) continue
            processNotification(sbn)
            processed++
        }
        if (processed > 0) {
            Log.d(TAG, "Back-filled $processed active notification(s)")
        }
    }

    private fun toRawNotification(sbn: StatusBarNotification): RawNotification? {
        val pkg = sbn.packageName ?: return null
        if (pkg == packageName) return null

        val notification = sbn.notification ?: return null
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return null

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
