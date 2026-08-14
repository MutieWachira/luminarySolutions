package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luminarysolutions.ui.common.UnifiedAchievementsScreen
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@Composable
fun VolunteerMainScreen(
    parentNavController: NavController,
    vm: VolunteerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            VolunteerBottomBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.VolunteerDashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.VolunteerDashboard.route) {
                VolunteerDashboardScreen(parentNavController, navController, vm)
            }
            composable(Screen.VolunteerExplore.route) {
                VolunteerExploreScreen(parentNavController, navController, vm)
            }
            composable(Screen.VolunteerAchievements.route) {
                UnifiedAchievementsScreen()
            }
            composable(Screen.VolunteerProfile.route) {
                VolunteerProfileScreen(parentNavController, vm)
            }
            composable(Screen.VolunteerNotifications.route) {
                VolunteerNotificationsScreen(vm)
            }
            composable(Screen.VolunteerTasks.route) {
                VolunteerTasksScreen(navController, vm)
            }
            composable(
                route = Screen.VolunteerTaskDetails.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                VolunteerTaskDetailsScreen(navController, taskId, vm)
            }
            composable(Screen.VolunteerEvents.route) {
                VolunteerEventsScreen(navController, vm)
            }
        }
    }
}

@Composable
fun VolunteerBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        val items = listOf(
            Triple(Screen.VolunteerDashboard.route, Icons.Default.Home, "Home"),
            Triple(Screen.VolunteerExplore.route, Icons.Default.Explore, "Explore"),
            Triple(Screen.VolunteerAchievements.route, Icons.Default.EmojiEvents, "Trophies"),
            Triple(Screen.VolunteerProfile.route, Icons.Default.Person, "Profile")
        )

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
