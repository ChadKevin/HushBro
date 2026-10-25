package com.invictus.hushbro.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.prefs.SettingsStore

@Composable
fun SettingsScreen(settingsStore: SettingsStore) {
    val settings by settingsStore.userSettingsFlow.collectAsState(initial = null)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        settings?.let { s ->
            Text("Retention: ${s.retentionDays} days", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text("Digest interval: ${s.digestInterval}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text("Quiet hours: ${s.quietHoursEnabled}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}
