package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.common.UnifiedAchievementsScreen
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.example.luminarysolutions.ui.navigation.Screen

@Composable
fun DonorMainScreen(
    parentNavController: NavController,
    vm: DonorViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            DonorBottomBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.DonorDashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.DonorDashboard.route) {
                DonorDashboardScreen(
                    navController = parentNavController,
                    onLoginClick = { /* Already logged in */ },
                    viewModel = hiltViewModel(),
                    isSubScreen = true
                )
            }
            composable(Screen.DonorDonations.route) {
                DonationHistoryScreen(parentNavController, vm, isSubScreen = true)
            }
            composable(Screen.DonorReports.route) {
                ImpactReportsScreen(parentNavController, vm, isSubScreen = true)
            }
            composable(Screen.DonorAchievements.route) {
                UnifiedAchievementsScreen()
            }
            composable(Screen.DonorProfile.route) {
                 DonorProfileScreen(parentNavController, vm)
            }
        }
    }
}

@Composable
fun DonorBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        val items = listOf(
            Triple(Screen.DonorDashboard.route, Icons.Default.Home, "Home"),
            Triple(Screen.DonorDonations.route, Icons.Default.History, "Donations"),
            Triple(Screen.DonorReports.route, Icons.Default.BarChart, "Impact"),
            Triple(Screen.DonorAchievements.route, Icons.Default.EmojiEvents, "Trophy"),
            Triple(Screen.DonorProfile.route, Icons.Default.Person, "Profile")
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
