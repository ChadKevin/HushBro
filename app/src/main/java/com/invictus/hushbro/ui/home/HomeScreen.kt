package com.invictus.hushbro.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.components.AutumnDivider
import com.invictus.hushbro.ui.components.CoffeeSteam
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.HeroCard
import com.invictus.hushbro.ui.components.NotificationCard
import com.invictus.hushbro.ui.components.ScreenHeader
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            HeroCard(
                title = "A little more peace.",
                subtitle = "Your notifications, with room to breathe.",
                accent = MaterialTheme.colorScheme.tertiary
            ) {
                CoffeeSteam(modifier = Modifier.width(40.dp), count = 3)
            }
            Spacer(Modifier.height(16.dp))
            NotificationAccessBanner(isAccessGranted = isAccessGranted, isListenerConnected = connected, onGrantClick = onGrantClick)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Captured", notifications.size.toString(), "Saved on this device", "☕", Modifier.weight(1f))
                StatCard("Unread", unread.toString(), "Waiting in your journal", "✨", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Apps", summaries.size.toString(), "Apps with history", "📱", Modifier.weight(1f))
                StatCard("Held", held.toString(), "Waiting for digest", "⏳", Modifier.weight(1f))
            }
            SectionHeader("A few useful things")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onNavigate("rules") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Create a rule") }
                OutlinedButton(onClick = { onNavigate("settings") }, modifier = Modifier.weight(1f)) { Text("Preferences") }
            }
            AutumnDivider()
            SectionHeader("Recently captured", "${notifications.size} total")
        }
        if (notifications.isEmpty()) {
            item { EmptyState("Your journal is clear", "When notifications arrive, they will appear here. Grant notification access to get started.") }
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
                OutlinedButton(onClick = { onNavigate("chronicle") }, modifier = Modifier.fillMaxWidth()) { Text("Open the full Chronicle") }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
