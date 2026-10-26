package com.invictus.hushbro

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.AppPrefRepository
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.notify.DigestManager
import com.invictus.hushbro.service.HushBroListenerService
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.nav.HushBroNav
import com.invictus.hushbro.ui.theme.AutumnTheme
import com.invictus.hushbro.util.CsvExporter
import com.invictus.hushbro.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var settingsStore: SettingsStore
    @Inject lateinit var listenerBridge: ListenerBridge
    @Inject lateinit var ruleRepository: RuleRepository
    @Inject lateinit var appPrefRepository: AppPrefRepository
    @Inject lateinit var digestManager: DigestManager
    @Inject lateinit var statsRepository: StatsRepository
    @Inject lateinit var csvExporter: CsvExporter

    private var notificationAccessGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationAccessGranted = PermissionUtils.isNotificationAccessGranted(this)
        setContent {
            AutumnTheme {
                HushBroNav(
                    notificationRepository = notificationRepository,
                    settingsStore = settingsStore,
                    listenerBridge = listenerBridge,
                    ruleRepository = ruleRepository,
                    statsRepository = statsRepository,
                    csvExporter = csvExporter,
                    isAccessGranted = notificationAccessGranted,
                    onGrantClick = { PermissionUtils.openNotificationAccessSettings(this@MainActivity) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationAccessGranted = PermissionUtils.isNotificationAccessGranted(this)
        if (notificationAccessGranted && !listenerBridge.isListenerConnected.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                runCatching {
                    NotificationListenerService.requestRebind(
                        ComponentName(this@MainActivity, HushBroListenerService::class.java)
                    )
                }
            }
        }
        listenerBridge.updateActiveCount()
    }
}
