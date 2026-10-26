package com.invictus.hushbro.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.components.AutumnDivider
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.HeroCard
import com.invictus.hushbro.ui.components.NotificationCard
import com.invictus.hushbro.ui.components.SectionHeader
import com.invictus.hushbro.ui.components.StatCard
import com.invictus.hushbro.ui.onboarding.NotificationAccessBanner
import java.text.DateFormat
import java.util.Date

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
    val unread = notifications.count { !it.isRead }
    val held = notifications.count { it.status.name == "HELD" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Spacer(Modifier.height(18.dp))
            Text("HUSHBRO  /  PERSONAL NOTIFICATION SPACE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("A quieter kind\nof phone.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(7.dp))
            Text("Keep what matters. Let the rest wait.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            HeroCard(
                title = if (isAccessGranted) "Your space is ready." else "Make room to breathe.",
                subtitle = if (isAccessGranted) "Your notification journal is on this device." else "Connect notification access to start building your private journal.",
                accent = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(12.dp))
            NotificationAccessBanner(isAccessGranted = isAccessGranted, isListenerConnected = connected, onGrantClick = onGrantClick)
            SectionHeader("Your overview", "ON THIS DEVICE")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Captured", notifications.size.toString(), "Saved notifications", "captured", Modifier.weight(1f))
                StatCard("Unread", unread.toString(), "Still to review", "unread", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Apps", summaries.size.toString(), "With saved history", "apps", Modifier.weight(1f))
                StatCard("Held", held.toString(), "Waiting for digest", "held", Modifier.weight(1f))
            }
            SectionHeader("Quick actions")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onNavigate("rules") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Create a rule")
                    Spacer(Modifier.size(6.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                OutlinedButton(onClick = { onNavigate("settings") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) { Text("Preferences") }
            }
            Spacer(Modifier.height(18.dp))
            AutumnDivider()
            SectionHeader("Recently captured", "${notifications.size} TOTAL")
        }
        if (notifications.isEmpty()) {
            item {
                EmptyState(
                    "Your journal starts here",
                    "When HushBro captures notifications, the latest entries will appear here. Your history stays on this device."
                )
            }
        } else {
            items(notifications.take(5), key = { it.id }) { n ->
                NotificationCard(
                    appLabel = n.appLabel,
                    title = n.title,
                    body = n.text,
                    metadata = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(n.postTime)),
                    unread = !n.isRead,
                    onClick = { onNavigate("chronicle") }
                )
            }
            item {
                OutlinedButton(onClick = { onNavigate("chronicle") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text("Open Chronicle")
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}
