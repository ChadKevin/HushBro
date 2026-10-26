package com.invictus.hushbro.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.ui.components.AutumnDivider
import com.invictus.hushbro.ui.components.EmptyState
import com.invictus.hushbro.ui.components.ScreenHeader
import com.invictus.hushbro.ui.components.SectionHeader
import com.invictus.hushbro.ui.components.WarmCard
import java.util.Calendar

@Composable
fun InsightsScreen(statsRepository: StatsRepository, notificationRepository: NotificationRepository) {
    val summaries by statsRepository.getAppSummaries().collectAsState(initial = emptyList())
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    val muted = notifications.count { it.status == NotificationStatus.MUTED }
    val held = notifications.count { it.status == NotificationStatus.HELD }
    val unread = notifications.count { !it.isRead }
    val important = notifications.count { it.isImportant }
    val dayCounts = (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
        val dayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val dayEnd = dayStart + 24L * 60 * 60 * 1000
        notifications.count { it.postTime >= dayStart && it.postTime < dayEnd }
    }
    val maxCount = (dayCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("YOUR WEEK, IN CONTEXT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        ScreenHeader("Insights", "A clear view of the notifications you receive and manage.")
        WarmCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Captured this week", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(dayCounts.sum().toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Notifications saved over the last 7 days", style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp).size(26.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                val gap = size.width / 13f
                val barWidth = gap * 1.35f
                val maxHeight = size.height - 8f
                repeat(7) { index ->
                    val height = (dayCounts[index].toFloat() / maxCount) * maxHeight
                    val x = gap + index * gap * 1.7f
                    drawRoundRect(
                        color = if (index == 6) androidx.compose.ui.graphics.Color(0xFF315B48) else androidx.compose.ui.graphics.Color(0xFFB7CBB7),
                        topLeft = Offset(x, size.height - height),
                        size = Size(barWidth, height.coerceAtLeast(4f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFE2E1D8),
                    start = Offset(0f, size.height - 1f),
                    end = Offset(size.width, size.height - 1f),
                    strokeWidth = 1f
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("6d ago", "5d", "4d", "3d", "2d", "Yesterday", "Today").forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        SectionHeader("Your notification mix")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InsightMetric("Unread", unread.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f))
            InsightMetric("Muted", muted.toString(), Icons.Filled.VolumeOff, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InsightMetric("Held", held.toString(), Icons.Filled.Schedule, Modifier.weight(1f))
            InsightMetric("Important", important.toString(), Icons.Filled.Star, Modifier.weight(1f))
        }
        SectionHeader("Apps in your journal", "${summaries.size} APPS")
        if (summaries.isEmpty()) {
            EmptyState("No patterns yet", "App activity will appear once HushBro starts capturing notifications.")
        } else {
            summaries.sortedByDescending { it.totalCount }.forEach { summary ->
                WarmCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Filled.Smartphone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(22.dp))
                        }
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(summary.appLabel, style = MaterialTheme.typography.titleSmall)
                            Text("${summary.unreadCount} unread · ${summary.totalCount} saved", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Canvas(Modifier.fillMaxWidth().height(5.dp)) {
                                val fraction = (summary.totalCount.toFloat() / (summaries.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(1)).coerceIn(0f, 1f)
                                drawRoundRect(androidx.compose.ui.graphics.Color(0xFFE2E9D9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                                drawRoundRect(androidx.compose.ui.graphics.Color(0xFF315B48), size = Size(size.width * fraction, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                            }
                        }
                        Text(summary.totalCount.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InsightMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    WarmCard(modifier) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
