package com.invictus.hushbro.notify

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigestManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository,
    private val settingsStore: SettingsStore
) {
    companion object {
        private const val PAGE_SIZE = 200
        private const val UPDATE_CHUNK_SIZE = 400 // below SQLite's common 999-bind-variable limit
        private const val DIGEST_NOTIFICATION_ID = 2001
        private const val PREVIEW_LINES = 8
    }

    suspend fun sendDigest(force: Boolean = false) {
        val settings = settingsStore.userSettingsFlow.first()
        if ((!force && settings.digestInterval == "OFF") || settings.digestTags.isEmpty()) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val digestChannel = notificationManager.getNotificationChannel(Channels.CHANNEL_ID_DIGEST)
            if (digestChannel?.importance == NotificationManager.IMPORTANCE_NONE) return
        }

        val now = System.currentTimeMillis()
        val tags = settings.digestTags.toList()
        val eligibleIds = ArrayList<Long>()
        val preview = ArrayList<String>(PREVIEW_LINES)
        var offset = 0

        while (true) {
            val page = notificationRepository.getHeldForDigestPage(now, tags, PAGE_SIZE, offset)
            if (page.isEmpty()) break
            for (item in page) {
                eligibleIds.add(item.id)
                if (preview.size < PREVIEW_LINES) {
                    val safeTitle = item.title?.takeIf { it.isNotBlank() } ?: "Notification"
                    preview.add("${item.appLabel}: $safeTitle")
                }
            }
            if (page.size < PAGE_SIZE) break
            offset += page.size
        }
        if (eligibleIds.isEmpty()) return

        val lines = buildString {
            append(preview.joinToString("\n"))
            if (eligibleIds.size > PREVIEW_LINES) append("\n…and ${eligibleIds.size - PREVIEW_LINES} more. Open HushBro to review the full digest.")
        }
        val notification = NotificationCompat.Builder(context, Channels.CHANNEL_ID_DIGEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HushBro Digest · ${eligibleIds.size} notifications")
            .setContentText("Held notifications are ready to review")
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        notificationManager.notify(DIGEST_NOTIFICATION_ID, notification)

        // Room/SQLite can reject one giant IN (...) statement on large histories.
        eligibleIds.chunked(UPDATE_CHUNK_SIZE).forEach { ids ->
            notificationRepository.updateStatusBatch(ids, NotificationStatus.DIGESTED)
        }
    }
}
