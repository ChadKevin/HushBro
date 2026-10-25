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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.invictus.hushbro.ui.nav.HushBroNav
import com.invictus.hushbro.ui.theme.AutumnTheme
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
            AutumnTheme {
                HushBroNav(
                    notificationRepository = notificationRepository,
                    settingsStore = settingsStore,
                    listenerBridge = listenerBridge,
                    ruleRepository = ruleRepository,
                    statsRepository = statsRepository,
                    isAccessGranted = PermissionUtils.isNotificationAccessGranted(this@MainActivity),
                    onGrantClick = { PermissionUtils.openNotificationAccessSettings(this@MainActivity) }
                )
            }
        }
    }
}

