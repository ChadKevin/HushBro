package com.invictus.hushbro.ui.nav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.util.PermissionUtils

sealed class Dest(val route: String, val label: String) {
    data object Home : Dest("home", "Home")
    data object Chronicle : Dest("chronicle", "Chronicle")
    data object Rules : Dest("rules", "Rules")
    data object Insights : Dest("insights", "Insights")
    data object Settings : Dest("settings", "Settings")
}

val destinations = listOf(Dest.Home, Dest.Chronicle, Dest.Rules, Dest.Insights, Dest.Settings)

@Composable
fun HushBroNav(
    notificationRepository: NotificationRepository,
    settingsStore: SettingsStore,
    listenerBridge: ListenerBridge,
    ruleRepository: RuleRepository,
    statsRepository: StatsRepository,
    isAccessGranted: Boolean,
    onGrantClick: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(navController = navController, startDestination = Dest.Home.route) {
            composable(Dest.Home.route) {
                com.invictus.hushbro.ui.home.HomeScreen(
                    notificationRepository = notificationRepository,
                    statsRepository = statsRepository,
                    listenerBridge = listenerBridge,
                    isAccessGranted = isAccessGranted,
                    onGrantClick = onGrantClick,
                    onNavigate = { navController.navigate(it) }
                )
            }
            composable(Dest.Chronicle.route) {
                com.invictus.hushbro.ui.history.ChronicleScreen(notificationRepository)
            }
            composable(Dest.Rules.route) {
                com.invictus.hushbro.ui.rules.RulesScreen(ruleRepository)
            }
            composable(Dest.Insights.route) {
                com.invictus.hushbro.ui.insights.InsightsScreen(statsRepository, notificationRepository)
            }
            composable(Dest.Settings.route) {
                com.invictus.hushbro.ui.settings.SettingsScreen(settingsStore)
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    NavigationBar {
        destinations.forEach { d ->
            NavigationBarItem(
                selected = current == d.route,
                onClick = { navController.navigate(d.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(androidx.compose.material.icons.Icons.Filled.Home, contentDescription = d.label) },
                label = { Text(d.label) }
            )
        }
    }
}
