package com.invictus.hushbro.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.work.DigestWorker
import com.invictus.hushbro.util.CsvExporter
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val DIGEST_WORK_NAME = "hushbro_digest_periodic"

@Composable
fun SettingsScreen(settingsStore: SettingsStore, csvExporter: CsvExporter) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsStore.userSettingsFlow.collectAsState(initial = null)
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
        ScreenHeader("Settings", "Make HushBro feel right for your day.")
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
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
