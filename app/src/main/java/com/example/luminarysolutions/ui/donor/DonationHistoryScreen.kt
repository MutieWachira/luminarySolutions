package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val total = vm.donations.sumOf { it.amount }
            Text("Total donated: KSh $total", fontWeight = FontWeight.SemiBold)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Campaign", fontWeight = FontWeight.Bold)
                        Text("Amount", fontWeight = FontWeight.Bold)
                    }

                    Divider()

                    if (vm.donations.isEmpty()) {
                        Text("No donations yet.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(vm.donations, key = { it.id }) { d ->
                                DonationRow(d)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonationRow(d: DonationUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(d.campaignTitle, fontWeight = FontWeight.SemiBold)
                Text("${d.date} • ${d.status}", style = MaterialTheme.typography.bodySmall)
                Text("Receipt: ${d.receiptRef}", style = MaterialTheme.typography.bodySmall)
            }
            Text("KSh ${d.amount}", fontWeight = FontWeight.Bold)
        }
    }
}