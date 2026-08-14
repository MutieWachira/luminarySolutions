package com.example.luminarysolutions.ui.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.navigation.Screen

/**
 * ClientBottomNav: Shared bottom navigation for the Client module.
 */
@Composable
fun ClientBottomNav(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.ClientDashboard.route,
            onClick = { 
                if (currentRoute != Screen.ClientDashboard.route) {
                    navController.navigate(Screen.ClientDashboard.route) {
                        popUpTo(Screen.ClientDashboard.route) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Dashboard, null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.ClientFreelanceServices.route,
            onClick = { 
                if (currentRoute != Screen.ClientFreelanceServices.route) {
                    navController.navigate(Screen.ClientFreelanceServices.route) 
                }
            },
            icon = { Icon(Icons.Default.Explore, null) },
            label = { Text("Services") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* My Projects/Applications */ },
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
            label = { Text("My Projects") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.ClientProfile.route,
            onClick = { 
                if (currentRoute != Screen.ClientProfile.route) {
                    navController.navigate(Screen.ClientProfile.route) 
                }
            },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Profile") }
        )
    }
}
