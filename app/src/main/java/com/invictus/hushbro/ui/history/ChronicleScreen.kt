package com.invictus.hushbro.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.ui.components.AutumnDivider
import com.invictus.hushbro.ui.components.CategoryChip
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.NotificationCard
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.ui.components.StatusChip
import com.invictus.hushbro.work.SnoozeWorker
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private enum class ChronicleFilter(val label: String) {
    ALL("All"), UNREAD("Unread"), IMPORTANT("Important"), HELD("Held"), MUTED("Muted"), SNOOZED("Snoozed")
}

@Composable
fun ChronicleScreen(notificationRepository: NotificationRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChronicleFilter.ALL) }
    var selected by remember { mutableStateOf<NotificationEntity?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val searchFlow = remember(query) { notificationRepository.search(query.trim()) }
    val notifications by searchFlow.collectAsState(initial = emptyList())
    val filtered = notifications.filter { n ->
        when (filter) {
            ChronicleFilter.ALL -> true
            ChronicleFilter.UNREAD -> !n.isRead
            ChronicleFilter.IMPORTANT -> n.isImportant
            ChronicleFilter.HELD -> n.status == NotificationStatus.HELD
            ChronicleFilter.MUTED -> n.status == NotificationStatus.MUTED
            ChronicleFilter.SNOOZED -> n.status == NotificationStatus.SNOOZED
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("YOUR NOTIFICATION JOURNAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        ScreenHeader("Chronicle", "Find the signal in everything that reached your phone.")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search apps, messages, keywords") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChronicleFilter.values().forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("${filtered.size} ENTRIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        AutumnDivider()
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState(if (query.isNotBlank()) "No matching notifications" else "Your Chronicle is quiet", if (query.isNotBlank()) "Try another keyword or switch to a different filter." else "Captured notifications will be organised here so you can return to them when it suits you.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { n ->
                    NotificationCard(
                        appLabel = n.appLabel,
                        title = n.title,
                        body = n.text,
                        metadata = DateFormat.getDateTimeInstance().format(Date(n.postTime)),
                        unread = !n.isRead,
                        onClick = { selected = n },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when {
                            n.isImportant -> StatusChip("important")
                            n.status == NotificationStatus.MUTED -> StatusChip("muted")
                            n.status == NotificationStatus.HELD -> StatusChip("held")
                            n.status == NotificationStatus.SNOOZED -> StatusChip("snoozed")
                            n.status == NotificationStatus.DIGESTED -> StatusChip("digested")
                            else -> StatusChip("kept")
                        }
                    }
                }
            }
        }
    }

    selected?.let { n ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(n.title?.ifBlank { n.appLabel } ?: n.appLabel, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    Text(n.appLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(n.text.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    Text("${n.tag} · ${n.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateFormat.getDateTimeInstance().format(Date(n.postTime)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (n.isImportant) Text("Marked important", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelLarge)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val request = OneTimeWorkRequestBuilder<SnoozeWorker>()
                        .setInitialDelay(10, TimeUnit.MINUTES)
                        .setInputData(Data.Builder().putString("title", n.title ?: n.appLabel).putString("text", n.text.orEmpty()).build())
                        .build()
                    WorkManager.getInstance(context).enqueueUniqueWork("hushbro_snooze_${n.id}", ExistingWorkPolicy.REPLACE, request)
                    scope.launch { notificationRepository.updateStatus(n.id, NotificationStatus.SNOOZED) }
                    Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
                    selected = null
                }) { Text("Snooze 10 min") }
            },
            dismissButton = {
                Row {
                    if (n.status == NotificationStatus.SNOOZED) {
                        TextButton(onClick = {
                            WorkManager.getInstance(context).cancelUniqueWork("hushbro_snooze_${n.id}")
                            scope.launch { notificationRepository.updateStatus(n.id, NotificationStatus.KEPT) }
                            selected = null
                        }) { Text("Unsnooze") }
                    }
                    TextButton(onClick = { selected = null }) { Text("Close") }
                    TextButton(onClick = {
                        scope.launch { notificationRepository.deleteById(n.id) }
                        selected = null
                    }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear your Chronicle?") },
            text = { Text("This permanently deletes the notification history stored by HushBro. Your rules and preferences will remain.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { notificationRepository.clearAll() }
                    confirmClear = false
                }) { Text("Delete history", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
        )
    }
}
