package com.invictus.hushbro.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.repo.NotificationRepository

@Composable
fun ChronicleScreen(notificationRepository: NotificationRepository) {
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) notifications else notifications.filter {
        it.title?.contains(query, true) == true || it.text?.contains(query, true) == true || it.appLabel.contains(query, true)
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chronicle", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        LazyColumn {
            items(filtered) { n ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Text(n.appLabel, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    Text(n.title ?: "", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Text(n.text ?: "", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(n.postTime)), style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
