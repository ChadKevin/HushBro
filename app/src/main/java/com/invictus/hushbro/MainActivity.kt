package com.invictus.hushbro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HushBroScreen(
                    notificationRepository = notificationRepository,
                    settingsStore = settingsStore
                )
            }
        }
    }
}

@Composable
fun HushBroScreen(
    notificationRepository: NotificationRepository,
    settingsStore: SettingsStore
) {
    val scope = rememberCoroutineScope()
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    val userSettings by settingsStore.userSettingsFlow.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HushBro is running!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hilt DI & Repositories: Active",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Stored notifications: ${notifications.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        userSettings?.let { settings ->
            Text(
                text = "DataStore: retention=${settings.retentionDays}d, digest=${settings.digestInterval}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            scope.launch {
                val now = System.currentTimeMillis()
                val testItem = NotificationEntity(
                    key = "sample_$now",
                    packageName = "com.invictus.hushbro",
                    appLabel = "HushBro",
                    title = "Test Alert",
                    text = "Sample notification saved via Repository",
                    postTime = now,
                    tag = NotificationTag.OTHER,
                    status = NotificationStatus.KEPT,
                    textHash = "hash_$now"
                )
                notificationRepository.insert(testItem)
            }
        }) {
            Text("Insert via Repository")
        }

        if (notifications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = {
                scope.launch {
                    notificationRepository.clearAll()
                }
            }) {
                Text("Clear All Notifications")
            }
        }
    }
}