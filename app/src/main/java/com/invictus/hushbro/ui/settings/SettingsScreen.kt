package com.invictus.hushbro.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.notify.Channels
import com.invictus.hushbro.notify.ListenerTestNotification
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.service.ListenerTestState
import com.invictus.hushbro.ui.components.AutumnDivider
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.work.DigestWorker
import com.invictus.hushbro.util.CsvExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val DIGEST_WORK_NAME = "hushbro_digest_periodic"

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    csvExporter: CsvExporter,
    listenerBridge: ListenerBridge,
    onGrantClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsStore.userSettingsFlow.collectAsState(initial = null)
    val listenerConnected by listenerBridge.isListenerConnected.collectAsState()
    val listenerTestState by listenerBridge.listenerTestState.collectAsState()

    val postListenerTest: () -> Unit = {
        try {
            listenerBridge.beginListenerTest()
            ListenerTestNotification.post(context)
            scope.launch {
                delay(5_000L)
                listenerBridge.markListenerTestFailed()
            }
        } catch (e: SecurityException) {
            listenerBridge.markListenerTestFailed()
            Toast.makeText(context, "Allow notifications for HushBro to run the test.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            listenerBridge.markListenerTestFailed()
            Toast.makeText(context, "Could not post the test notification.", Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) postListenerTest()
        else {
            listenerBridge.markListenerTestFailed()
            Toast.makeText(context, "Notification permission is needed for the test.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(settings?.digestInterval) {
        settings?.digestInterval?.let { scheduleDigestWork(context, it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val file = csvExporter.export()
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        file.inputStream().use { input -> input.copyTo(output) }
                    } ?: error("Could not open the selected file")
                    Toast.makeText(context, "CSV exported", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.localizedMessage ?: "unknown error"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("PREFERENCES  /  PRIVACY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        ScreenHeader("Settings", "Your device, your boundaries, your data.")

        SettingsCard(
            "Notification listener",
            "Check that Android is connected to HushBro and that notification callbacks are reaching the app."
        ) {
            Text(
                if (listenerConnected) "Status: Connected" else "Status: Disconnected",
                style = MaterialTheme.typography.titleSmall,
                color = if (listenerConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(6.dp))
            Text(
                when (listenerTestState) {
                    ListenerTestState.IDLE -> "Self-test has not been run."
                    ListenerTestState.WAITING -> "Waiting for HushBro to receive and dismiss its quiet test notification…"
                    ListenerTestState.PASSED -> "Test passed. HushBro received the callback and dismissed the test notification."
                    ListenerTestState.FAILED -> "No callback was confirmed. Check Notification Access in Android Settings, then try again."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        postListenerTest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send test notification")
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = onGrantClick, modifier = Modifier.fillMaxWidth()) {
                Text("Open Notification Access settings")
            }
        }
        AutumnDivider()

        settings?.let { s ->
            SettingsCard("Digest schedule", "Choose when HushBro checks for notifications held by your rules.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("OFF", "DAILY", "WEEKLY").forEach { interval ->
                        FilterChip(
                            selected = s.digestInterval == interval,
                            onClick = {
                                scope.launch { settingsStore.setDigestInterval(interval) }
                                scheduleDigestWork(context, interval)
                            },
                            label = { Text(if (interval == "OFF") "Off" else interval.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Digest delivery uses Android WorkManager, so it may run later than the selected daily or weekly interval.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text("Include these notification groups", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(NotificationTag.PROMO, NotificationTag.SOCIAL, NotificationTag.OTP, NotificationTag.OTHER).forEach { tag ->
                        FilterChip(
                            selected = tag.name in s.digestTags,
                            onClick = {
                                val updated = s.digestTags.toMutableSet().apply {
                                    if (contains(tag.name)) remove(tag.name) else add(tag.name)
                                }
                                scope.launch { settingsStore.setDigestTags(updated) }
                            },
                            label = { Text(tag.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }
                        )
                    }
                }
            }
            AutumnDivider()

            SettingsCard("Quiet hours", "Notifications arriving during this window are held for a later digest, unless a mute rule matches.") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Enable quiet hours", style = MaterialTheme.typography.titleMedium)
                        Text("Overnight times are supported.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = s.quietHoursEnabled, onCheckedChange = { enabled -> scope.launch { settingsStore.setQuietHoursEnabled(enabled) } })
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {
                        val hour = s.quietHoursStart / 60
                        val minute = s.quietHoursStart % 60
                        TimePickerDialog(context, { _, h, m -> scope.launch { settingsStore.setQuietHours(h * 60 + m, s.quietHoursEnd) } }, hour, minute, true).show()
                    }, modifier = Modifier.weight(1f)) { Text("Starts ${formatMinutes(s.quietHoursStart)}") }
                    OutlinedButton(onClick = {
                        val hour = s.quietHoursEnd / 60
                        val minute = s.quietHoursEnd % 60
                        TimePickerDialog(context, { _, h, m -> scope.launch { settingsStore.setQuietHours(s.quietHoursStart, h * 60 + m) } }, hour, minute, true).show()
                    }, modifier = Modifier.weight(1f)) { Text("Ends ${formatMinutes(s.quietHoursEnd)}") }
                }
            }
            AutumnDivider()

            SettingsCard("Your data", "Your notification journal is stored locally. Export only to a location you choose.") {
                Button(onClick = { exportLauncher.launch("hushbro_export.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Export notification history (CSV)") }
                Spacer(Modifier.height(6.dp))
                Text("The CSV includes notification titles and message text stored by HushBro. Keep the exported file private.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            SettingsCard("About HushBro", "A quieter way to manage notifications.") {
                Text("HushBro uses notification access to capture notification details, apply local rules, and keep a searchable journal on this device.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(5.dp))
                Text("Version 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, description: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                val icon = when (title) {
                    "Digest schedule" -> Icons.Filled.Schedule
                    "Quiet hours" -> Icons.Filled.DoNotDisturbOn
                    "Your data" -> Icons.Filled.Storage
                    else -> Icons.Filled.Info
                }
                androidx.compose.material3.Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(9.dp).size(21.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(3.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(15.dp))
            content()
        }
    }
}

private fun formatMinutes(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

private fun scheduleDigestWork(context: android.content.Context, interval: String) {
    val workManager = WorkManager.getInstance(context)
    if (interval == "OFF") {
        workManager.cancelUniqueWork(DIGEST_WORK_NAME)
        return
    }
    val days = if (interval == "WEEKLY") 7L else 1L
    val request = PeriodicWorkRequestBuilder<DigestWorker>(days, TimeUnit.DAYS).build()
    workManager.enqueueUniquePeriodicWork(DIGEST_WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
}
