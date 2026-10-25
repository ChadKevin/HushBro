package com.invictus.hushbro.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.MetricCard
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.ui.components.SectionHeader
import com.invictus.hushbro.ui.components.WarmCard

@Composable
fun InsightsScreen(statsRepository: StatsRepository, notificationRepository: NotificationRepository) {
    val summaries by statsRepository.getAppSummaries().collectAsState(initial = emptyList())
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    val muted = notifications.count { it.status == NotificationStatus.MUTED }
    val held = notifications.count { it.status == NotificationStatus.HELD }
    val unread = notifications.count { !it.isRead }
    val important = notifications.count { it.isImportant }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Spacer(Modifier.height(12.dp))
            ScreenHeader("Your patterns", "A gentle overview based on the notifications stored on this device.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Captured", notifications.size.toString(), "All stored records", Modifier.weight(1f))
                MetricCard("Unread", unread.toString(), "Not opened in Chronicle", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Muted", muted.toString(), "Matched by mute rules", Modifier.weight(1f))
                MetricCard("Held", held.toString(), "Waiting for a digest", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Important", important.toString(), "Marked by rules", Modifier.weight(1f))
                MetricCard("Apps", summaries.size.toString(), "Apps represented", Modifier.weight(1f))
            }
            SectionHeader("Apps in your journal")
        }
        if (summaries.isEmpty()) {
            item { EmptyState("No patterns yet", "App summaries will appear after HushBro captures notifications.") }
        } else {
            items(summaries, key = { it.packageName }) { summary ->
                WarmCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(summary.appLabel, style = MaterialTheme.typography.titleMedium)
                            Text(summary.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(summary.totalCount.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("${summary.unreadCount} unread", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
