package com.invictus.hushbro.notify

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
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
        val notifications = notificationRepository.getAllNotifications().first()
        val eligible = notifications.filter { it.tag.name in settings.digestTags }
        if (eligible.isEmpty()) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, Channels.CHANNEL_ID_DIGEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HushBro Digest")
            .setContentText("${eligible.size} notifications captured")
            .setStyle(NotificationCompat.BigTextStyle().bigText(eligible.joinToString("\n") { "${it.appLabel}: ${it.title ?: ""}" }))
            .build()
        nm.notify(2001, notification)
    }
}
