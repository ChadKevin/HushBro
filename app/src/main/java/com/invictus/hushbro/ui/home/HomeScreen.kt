package com.invictus.hushbro.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.components.AppScaffold
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.ui.onboarding.NotificationAccessBanner

@Composable
fun HomeScreen(
    notificationRepository: NotificationRepository,
    statsRepository: StatsRepository,
    listenerBridge: ListenerBridge,
    isAccessGranted: Boolean,
    onGrantClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    val summaries by statsRepository.getAppSummaries().collectAsState(initial = emptyList())
    val connected by listenerBridge.isListenerConnected.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("A little more peace today.", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text("HushBro helps make notifications manageable.", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        NotificationAccessBanner(isAccessGranted = isAccessGranted, isListenerConnected = connected, onGrantClick = onGrantClick)
        Spacer(Modifier.height(12.dp))
        Text("Captured: ${notifications.size}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Text("Apps: ${summaries.size}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text("Recent", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(notifications.take(20)) { n ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(n.appLabel, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    Text(n.title ?: "", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Text(n.text ?: "", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
