package com.invictus.hushbro.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.invictus.hushbro.service.HushBroListenerService

object PermissionUtils {

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledPackages.contains(context.packageName)) {
            return true
        }
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val expectedComponent = ComponentName(context, HushBroListenerService::class.java).flattenToString()
        return flat?.contains(expectedComponent) == true || flat?.contains(context.packageName) == true
    }

    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
