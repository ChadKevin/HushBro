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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.onboarding.NotificationAccessBanner
import com.invictus.hushbro.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var settingsStore: SettingsStore

    @Inject
    lateinit var listenerBridge: ListenerBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HushBroScreen(
                    notificationRepository = notificationRepository,
                    settingsStore = settingsStore,
                    listenerBridge = listenerBridge
                )
            }
        }
    }
}

@Composable
fun HushBroScreen(
    notificationRepository: NotificationRepository,
    settingsStore: SettingsStore,
    listenerBridge: ListenerBridge
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var isAccessGranted by remember {
        mutableStateOf(PermissionUtils.isNotificationAccessGranted(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessGranted = PermissionUtils.isNotificationAccessGranted(context)
                listenerBridge.updateActiveCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isListenerConnected by listenerBridge.isListenerConnected.collectAsState()
    val liveNotificationCount by listenerBridge.activeNotificationCount.collectAsState()
    val notifications by notificationRepository.getAllNotifications().collectAsState(initial = emptyList())
    val userSettings by settingsStore.userSettingsFlow.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "HushBro",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        NotificationAccessBanner(
            isAccessGranted = isAccessGranted,
            isListenerConnected = isListenerConnected,
            onGrantClick = {
                PermissionUtils.openNotificationAccessSettings(context)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Offline Notification Manager",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Live notifications in shade: $liveNotificationCount",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Stored notifications in DB: ${notifications.size}",
            style = MaterialTheme.typography.bodyMedium
        )

        userSettings?.let { settings ->
            Text(
                text = "Settings: retention=${settings.retentionDays}d, digest=${settings.digestInterval}",
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
            Text("Insert Test Notification")
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