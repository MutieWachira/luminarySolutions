package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDashboardScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    val totalDonated = vm.donations.sumOf { it.amount }
    val supported = vm.donations.map { it.campaignTitle }.distinct().count()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Donor Dashboard") })
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Welcome!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Your impact and transparency view.", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Total Donated", "KSh $totalDonated", Modifier.weight(1f))
                StatCard("Campaigns Supported", supported.toString(), Modifier.weight(1f))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.SemiBold)

                    Button(
                        onClick = { navController.navigate(Screen.DonorCampaigns.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Browse Campaigns")
                    }

                    Button(
                        onClick = { navController.navigate(Screen.DonorDonations.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Donation History")
                    }

                    Button(
                        onClick = { navController.navigate(Screen.DonorReports.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Impact Reports")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}