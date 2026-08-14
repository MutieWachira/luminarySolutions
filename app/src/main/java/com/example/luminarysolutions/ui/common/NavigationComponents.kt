package com.example.luminarysolutions.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ExecutiveNavy = Color(0xFF0F172A)
private val ActionIndigo = Color(0xFF6366F1)

@Composable
fun ExecutiveNavigationBar(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToReports: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            NavigationBarItem(
                selected = currentScreen == "home",
                onClick = onNavigateToHome,
                icon = { Icon(Icons.Default.GridView, "Home") },
                label = { Text("Home", fontWeight = if (currentScreen == "home") FontWeight.Bold else FontWeight.Normal) },
                colors = navigationItemColors()
            )
            NavigationBarItem(
                selected = currentScreen == "projects",
                onClick = onNavigateToProjects,
                icon = { Icon(Icons.Default.AccountTree, "Projects") },
                label = { Text("Pipeline", fontWeight = if (currentScreen == "projects") FontWeight.Bold else FontWeight.Normal) },
                colors = navigationItemColors()
            )
            
            // Central Add Action
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(ExecutiveNavy, CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                }
            }

            NavigationBarItem(
                selected = currentScreen == "reports",
                onClick = onNavigateToReports,
                icon = { Icon(Icons.Default.BarChart, "Reports") },
                label = { Text("Reports", fontWeight = if (currentScreen == "reports") FontWeight.Bold else FontWeight.Normal) },
                colors = navigationItemColors()
            )
            NavigationBarItem(
                selected = currentScreen == "profile",
                onClick = { },
                icon = { Icon(Icons.Default.AccountCircle, "Settings") },
                label = { Text("Profile", fontWeight = if (currentScreen == "profile") FontWeight.Bold else FontWeight.Normal) },
                colors = navigationItemColors()
            )
        }
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ActionIndigo,
    selectedTextColor = ActionIndigo,
    indicatorColor = ActionIndigo.copy(alpha = 0.1f),
    unselectedIconColor = Color.Gray,
    unselectedTextColor = Color.Gray
)
