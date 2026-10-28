package com.invictus.hushbro.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.invictus.hushbro.data.prefs.SettingsStore
import com.invictus.hushbro.data.repo.NotificationRepository
import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.data.repo.StatsRepository
import com.invictus.hushbro.service.ListenerBridge
import com.invictus.hushbro.ui.history.ChronicleScreen
import com.invictus.hushbro.ui.home.HomeScreen
import com.invictus.hushbro.ui.insights.InsightsScreen
import com.invictus.hushbro.ui.rules.RulesScreen
import com.invictus.hushbro.ui.settings.SettingsScreen
import com.invictus.hushbro.util.CsvExporter

private data class Destination(val route: String, val label: String, val icon: ImageVector)
private val destinations = listOf(
    Destination("home", "Home", Icons.Filled.Home),
    Destination("chronicle", "Chronicle", Icons.Filled.Search),
    Destination("rules", "Rules", Icons.Filled.CheckCircle),
    Destination("insights", "Insights", Icons.Filled.BarChart),
    Destination("settings", "Settings", Icons.Filled.Settings)
)

@Composable
fun HushBroNav(
    notificationRepository: NotificationRepository,
    settingsStore: SettingsStore,
    listenerBridge: ListenerBridge,
    ruleRepository: RuleRepository,
    statsRepository: StatsRepository,
    csvExporter: CsvExporter,
    isAccessGranted: Boolean,
    onGrantClick: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("home") {
                 HomeScreen(notificationRepository, statsRepository, listenerBridge, isAccessGranted, onGrantClick) { navController.navigate(it) }
            }
            composable("chronicle") { ChronicleScreen(notificationRepository) }
            composable("rules") { RulesScreen(ruleRepository) }
            composable("insights") { InsightsScreen(statsRepository, notificationRepository) }
            composable("settings") { SettingsScreen(settingsStore, csvExporter, listenerBridge, onGrantClick) }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = current == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
