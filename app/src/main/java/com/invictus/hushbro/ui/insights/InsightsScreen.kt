package com.invictus.hushbro.ui.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository

@Composable
fun InsightsScreen(statsRepository: StatsRepository, notificationRepository: NotificationRepository) {
    val summaries by statsRepository.getAppSummaries().collectAsState(initial = emptyList())
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Insights", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Text("Captured: ${notifications.size}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        summaries.forEach { s ->
            Text("${s.appLabel}: ${s.totalCount} total, ${s.unreadCount} unread", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}
