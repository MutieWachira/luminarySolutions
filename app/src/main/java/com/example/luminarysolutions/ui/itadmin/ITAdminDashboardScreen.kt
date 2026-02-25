package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ITAdminDashboardScreen(
    navController: NavController,
    role: UserRole,
    viewModel: LoginViewModel? = null // optional for Preview
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IT Admin Dashboard") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Platform Overview",
                style = MaterialTheme.typography.headlineSmall
            )

            // ✅ quick stats (UI-only)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard("Users", "48", Modifier.weight(1f))
                StatCard("Alerts", "3", Modifier.weight(1f))
                StatCard("Logs Today", "120", Modifier.weight(1f))
            }

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium
            )

            val actions = listOf(
                ITAction("Users", "Manage accounts", Icons.Default.Badge, Screen.Users.route),
                ITAction("Roles", "Permissions matrix", Icons.Default.AdminPanelSettings, Screen.Roles.route),
                ITAction("Audit Logs", "Track activity", Icons.Default.Visibility, Screen.AuditLogs.route),
                ITAction("System Settings", "Security & config", Icons.Default.Settings, Screen.SystemSettings.route),
            )

            // ✅ 2x2 GRID (same ActionCard design)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // enough for 2 rows
            ) {
                items(actions) { action ->
                    ActionCard(action = action) {
                        navController.navigate(action.route)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Security Status", style = MaterialTheme.typography.titleMedium)
                    Text("• MFA: Enabled", style = MaterialTheme.typography.bodyMedium)
                    Text("• Session Timeout: 15 minutes", style = MaterialTheme.typography.bodyMedium)
                    Text("• Maintenance Mode: OFF", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ActionCard(action: ITAction, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // fits grid cell
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(action.icon, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(action.title, style = MaterialTheme.typography.titleMedium)
                Text(action.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private data class ITAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@Preview(showBackground = true)
@Composable
fun ITAdminDashboardScreenPreview() {
    ITAdminDashboardScreen(
        navController = rememberNavController(),
        role = UserRole.ADMIN
    )
}