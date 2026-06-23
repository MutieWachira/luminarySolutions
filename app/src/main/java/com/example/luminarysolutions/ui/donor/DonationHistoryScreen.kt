package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel(),
    isSubScreen: Boolean = false
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { 
        vm.loadUserStats(FirebaseAuth.getInstance().currentUser?.uid ?: "me") 
    }

    if (isSubScreen) {
        DonationHistoryContent(uiState)
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Donation History") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                DonationHistoryContent(uiState)
            }
        }
    }
}

@Composable
fun DonationHistoryContent(uiState: com.example.luminarysolutions.ui.donor.viewmodel.DonorUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val total = uiState.donations.sumOf { it.amount }
        Text("Total donated: KSh $total", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Campaign", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Amount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (uiState.donations.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No donations yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.donations, key = { it.id }) { d ->
                            DonationRow(d)
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