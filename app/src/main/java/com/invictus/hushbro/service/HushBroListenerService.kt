package com.invictus.hushbro.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.invictus.hushbro.notify.ListenerTestNotification
import com.invictus.hushbro.data.db.model.RuleAction
import com.invictus.hushbro.notify.ListenerTestNotification
import com.invictus.hushbro.pipeline.ImmediateRuleMatcher
import com.invictus.hushbro.pipeline.NotificationPipeline
import com.invictus.hushbro.pipeline.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class HushBroListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HushBroListener"
    }

    @Inject lateinit var listenerBridge: ListenerBridge
    @Inject lateinit var pipeline: NotificationPipeline

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appLabelCache = ConcurrentHashMap<String, String>()
    @Inject lateinit var immediateRuleMatcher: ImmediateRuleMatcher

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        listenerBridge.attachService(this)
        // A device can already have hundreds or thousands of active notifications.
        // Backfill off the service callback thread to avoid blocking Android's main thread.
        backgroundScope.launch { backfillActiveNotifications() }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        listenerBridge.detachService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { requestRebind(android.content.ComponentName(this, HushBroListenerService::class.java)) }
                .onFailure { Log.w(TAG, "Could not request notification-listener rebind", it) }
            runCatching {
                requestRebind(ComponentName(this, HushBroListenerService::class.java))
            }.onFailure {
                Log.w(TAG, "Could not request notification-listener rebind", it)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Enqueue for the pipeline; the Room-backed rule fallback in the writer
        // evaluates rules rather than doing immediate in-service matching.
        processNotification(sbn, updateCount = true)
        processNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return
        listenerBridge.updateActiveCount()
    }

    private fun processNotification(sbn: StatusBarNotification?, updateCount: Boolean) {
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
            if (updateCount) listenerBridge.updateActiveCount()
            return
        }

        val raw = toRawNotification(sbn) ?: return

        // Immediate in-service rule matching is not yet implemented
        // (ImmediateRuleMatcher is not part of the current pipeline). Leave the
        // event unmarked so the Room-backed rule fallback in the writer can
        // evaluate it rather than treating it as already handled.
        if (!pipeline.enqueue(raw)) {
            Log.e(TAG, "Notification queue is full; event could not be queued (pkg=${raw.packageName})")
        }

        if (updateCount) listenerBridge.updateActiveCount()
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

    private fun backfillActiveNotifications() {
        val active = listenerBridge.getActiveNotifications()
        var processed = 0
        for (sbn in active) {
            if (sbn.packageName == packageName) continue
            processNotification(sbn, updateCount = false)
            processed++
        }
        listenerBridge.updateActiveCount()
        if (processed > 0) Log.i(TAG, "Back-filled $processed active notification(s)")
    }

    private fun toRawNotification(sbn: StatusBarNotification): RawNotification? {
        val pkg = sbn.packageName ?: return null
        if (pkg == packageName) return null

        val notification = sbn.notification ?: return null
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return null

        val appLabel = appLabelCache.getOrPut(pkg) {
            runCatching {
                val info = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(pkg)
        }

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
            title = notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extractMessageText(notification),
            postTime = sbn.postTime,
            category = notification.category,
            visibility = notification.visibility
        )
    }

    /** Handles basic notifications, BigText, inbox-style lines and MessagingStyle. */
    private fun extractMessageText(notification: Notification): String? {
        val extras = notification.extras ?: return null
        val direct = listOf(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        ).firstOrNull { !it.isNullOrBlank() }
        if (!direct.isNullOrBlank()) return direct

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val messageArray = runCatching {
                extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            }.getOrNull()
            val latestMessage = messageArray
                ?.asSequence()
                ?.mapNotNull { it as? Bundle }
                ?.mapNotNull { it.getCharSequence("text")?.toString() }
                ?.lastOrNull { it.isNotBlank() }
            if (!latestMessage.isNullOrBlank()) return latestMessage
        }

        return extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString() }
            ?.filter { it.isNotBlank() }
            ?.joinToString("\n")
            ?.takeIf { it.isNotBlank() }
    }

    override fun onDestroy() {
        backgroundScope.cancel()
        super.onDestroy()
    }
}
