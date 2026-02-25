package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailsScreen(
    navController: NavController,
    campaignId: String,
    vm: DonorViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    val campaign = remember(vm.campaigns, campaignId) { vm.getCampaign(campaignId) }

    if (campaign == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Campaign Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Campaign not found.")
            }
        }
        return
    }

    var amountText by remember { mutableStateOf("") }

    // snack-style message
    val msg = vm.uiMessage
    LaunchedEffect(msg) {
        if (msg != null) {
            // auto clear after showing once
            vm.clearMessage()
        }
    }

    val progress = if (campaign.goalAmount == 0) 0f else (campaign.raisedAmount.toFloat() / campaign.goalAmount).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campaign Details") },
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
            Text(campaign.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${campaign.category} • ${campaign.location}")
            Text("Last update: ${campaign.lastUpdate}")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Funding Progress", fontWeight = FontWeight.SemiBold)
                    Text("Raised: KSh ${campaign.raisedAmount} / ${campaign.goalAmount}")
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                    Text("Progress: ${(progress * 100).roundToInt()}%")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Donate", fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter(Char::isDigit) },
                        label = { Text("Amount (KSh)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val amt = amountText.toIntOrNull() ?: 0
                            vm.donate(userId = "me", campaignId = campaign.id, amount = amt)
                            amountText = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Donate Now (Demo)")
                    }

                    Text(
                        "Note: This is demo mode. Later we’ll connect M-Pesa/Flutterwave and verify payment.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}