package com.invictus.hushbro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import com.invictus.hushbro.data.repo.AppPrefRepository
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.util.CsvExporter
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.notify.DigestManager
import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.onboarding.NotificationAccessBanner
import com.invictus.hushbro.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    @Inject
    lateinit var ruleRepository: RuleRepository

    @Inject
    lateinit var appPrefRepository: AppPrefRepository

    @Inject
    lateinit var digestManager: DigestManager

    @Inject
    lateinit var statsRepository: StatsRepository

    @Inject
    lateinit var csvExporter: CsvExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HushBroScreen(
                    notificationRepository = notificationRepository,
                    settingsStore = settingsStore,
                    listenerBridge = listenerBridge,
                    ruleRepository = ruleRepository,
                    appPrefRepository = appPrefRepository,
                    digestManager = digestManager,
                    statsRepository = statsRepository,
                    csvExporter = csvExporter
                )
            }
        }
    }
}

@Composable
fun HushBroScreen(
    notificationRepository: NotificationRepository,
    settingsStore: SettingsStore,
    listenerBridge: ListenerBridge,
    ruleRepository: RuleRepository,
    appPrefRepository: AppPrefRepository,
    digestManager: DigestManager,
    statsRepository: StatsRepository,
    csvExporter: CsvExporter
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
    var searchQuery by remember { mutableStateOf("") }
    val filteredNotifications = remember(notifications, searchQuery) {
        if (searchQuery.isBlank()) notifications else notifications.filter {
            it.title?.contains(searchQuery, true) == true ||
                it.text?.contains(searchQuery, true) == true ||
                it.appLabel.contains(searchQuery, true)
        }
    }
    val userSettings by settingsStore.userSettingsFlow.collectAsState(initial = null)
    val rules by ruleRepository.observeRules().collectAsState(initial = emptyList())
    val appSummaries by statsRepository.getAppSummaries().collectAsState(initial = emptyList())

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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search notifications") },
            modifier = Modifier.fillMaxWidth()
        )

        if (filteredNotifications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredNotifications) { n ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${n.appLabel}: ${n.title ?: ""}", style = MaterialTheme.typography.titleSmall)
                        Text(text = n.text ?: "", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = {
                            val data = Data.Builder()
                                .putString("title", n.title ?: "")
                                .putString("text", n.text ?: "")
                                .build()
                            val req = OneTimeWorkRequestBuilder<com.invictus.hushbro.work.SnoozeWorker>()
                                .setInitialDelay(10, java.util.concurrent.TimeUnit.MINUTES)
                                .setInputData(data)
                                .build()
                            WorkManager.getInstance(context).enqueue(req)
                        }) { Text("Snooze 10m") }
                    }
                }
            }
        }

        var ruleKeyword by remember { mutableStateOf("") }
        var ruleApp by remember { mutableStateOf("") }
        var ruleAction by remember { mutableStateOf(com.invictus.hushbro.data.db.model.RuleAction.MUTE) }

        OutlinedTextField(value = ruleKeyword, onValueChange = { ruleKeyword = it }, label = { Text("Rule keyword") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ruleApp, onValueChange = { ruleApp = it }, label = { Text("App package (optional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                ruleRepository.upsert(
                    com.invictus.hushbro.data.db.entity.RuleEntity(
                        priority = rules.size,
                        appPackage = ruleApp.ifBlank { null },
                        keyword = ruleKeyword,
                        action = ruleAction
                    )
                )
                ruleKeyword = ""
                ruleApp = ""
            }
        }) { Text("Add Rule") }

        rules.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${r.priority}: ${r.keyword} -> ${r.action}", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { scope.launch { ruleRepository.upsert(r.copy(enabled = !r.enabled)) } }) {
                    Text(if (r.enabled) "Disable" else "Enable")
                }
                OutlinedButton(onClick = { scope.launch { ruleRepository.deleteById(r.id) } }) { Text("Delete") }
            }
        }
        userSettings?.let { settings ->
            Text(
                text = "Settings: retention=${settings.retentionDays}d, digest=${settings.digestInterval}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Text(text = "Insights:", style = MaterialTheme.typography.titleMedium)
        appSummaries.forEach { s ->
            Text(text = "${s.appLabel}: ${s.totalCount} total, ${s.unreadCount} unread", style = MaterialTheme.typography.bodySmall)
        }

        Button(onClick = { scope.launch { val f = csvExporter.export(); android.widget.Toast.makeText(context, "Exported: ${f.absolutePath}", android.widget.Toast.LENGTH_SHORT).show() } }) { Text("Export CSV") }

        Button(onClick = { scope.launch { digestManager.sendDigest() } }) { Text("Send Digest Now") }
        Button(onClick = {
            val request = PeriodicWorkRequestBuilder<com.invictus.hushbro.work.DigestWorker>(24, java.util.concurrent.TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "digest", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }) { Text("Schedule Daily Digest") }

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