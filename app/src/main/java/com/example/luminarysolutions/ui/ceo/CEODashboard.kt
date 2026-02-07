package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

// 1️⃣ The main dashboard screen
@Composable
fun CEODashboardScreen(
    navController: NavController,
    role: UserRole,
    viewModel: LoginViewModel
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        Text(
            text = "Welcome, CEO!",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Here’s an overview of your company’s performance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // KPI Cards
        val kpis = listOf(
            KPICardData("Total Projects", "25", Icons.Default.Assessment),
            KPICardData("Budget Utilized", "$1.2M", Icons.Default.AccountBalance),
            KPICardData("Impact Score", "85%", Icons.Default.Public)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(kpis) { kpi ->
                KPICard(title = kpi.title, value = kpi.value, icon = kpi.icon)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Business module buttons
        val modules = listOf(
            DashboardModule("Projects & Operations", Screen.Projects.route, Icons.Default.Assessment),
            DashboardModule("Finance & Reports", Screen.Finance.route, Icons.Default.AccountBalance),
            DashboardModule("Partners & Donors", Screen.Partners.route, Icons.Default.People),
            DashboardModule("Community & Programs", Screen.Community.route, Icons.Default.Public)
        )


        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            modules.forEach { module ->
                Button(
                    onClick = { navController.navigate(module.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(module.icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(module.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                viewModel.resetLoginState()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.CEODashboard.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = Color.White)
        }



    }
}

// KPI Card
@Composable
fun KPICard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CEODashboardScreenPreview() {
    CEODashboardScreen(navController = rememberNavController(), role = UserRole.CEO, viewModel = LoginViewModel())
}


// Data classes
data class KPICardData(val title: String, val value: String, val icon: ImageVector)
data class DashboardModule(val name: String, val route: String, val icon: ImageVector)
