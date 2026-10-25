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
    suspend fun sendDigest() {
        val settings = settingsStore.userSettingsFlow.first()
        if (settings.digestInterval == "OFF") return
        val notifications = notificationRepository.getAllNotifications().first()
        val eligible = notifications.filter {
            it.status == NotificationStatus.HELD && it.tag.name in settings.digestTags
        }
        if (eligible.isEmpty()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val digestText = eligible.joinToString("\n") { item ->
            val safeTitle = item.title?.takeIf { it.isNotBlank() } ?: "Notification"
            "${item.appLabel}: $safeTitle"
        }
        val notification = NotificationCompat.Builder(context, Channels.CHANNEL_ID_DIGEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HushBro Digest")
            .setContentText("${eligible.size} notifications are ready")
            .setStyle(NotificationCompat.BigTextStyle().bigText(digestText))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(2001, notification)
        notificationRepository.updateStatusBatch(eligible.map { it.id }, NotificationStatus.DIGESTED)
    }
}
