package com.example.luminarysolutions.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.navigation.Screen

@Composable
fun AdminMainScreen(
    parentNavController: NavController
) {
    val adminNavController = rememberNavController()
    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        AdminNavItem("Dashboard", Screen.AdminDashboard.route, Icons.Default.Dashboard),
        AdminNavItem("Users", Screen.Users.route, Icons.Default.Group),
        AdminNavItem("Logs", Screen.AuditLogs.route, Icons.Default.History),
        AdminNavItem("Settings", Screen.SystemSettings.route, Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.route == item.route,
                        onClick = {
                            adminNavController.navigate(item.route) {
                                popUpTo(adminNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = adminNavController,
            startDestination = Screen.AdminDashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(navController = adminNavController)
            }
            composable(Screen.Users.route) {
                UserManagementScreen(navController = adminNavController)
            }
            composable(Screen.AuditLogs.route) {
                AuditLogsScreen(navController = adminNavController)
            }
            composable(Screen.SystemSettings.route) {
                SystemSettingsScreen(
                    navController = adminNavController,
                    rootNavController = parentNavController
                )
            }
            composable(Screen.Roles.route) {
                RolesScreen(navController = adminNavController)
            }
        }
    }
}

data class AdminNavItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
