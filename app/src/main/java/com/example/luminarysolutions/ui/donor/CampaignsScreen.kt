package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    var search by remember { mutableStateOf("") }

    val list = remember(vm.campaigns, search) {
        vm.campaigns.filter {
            it.title.contains(search, true) ||
                    it.category.contains(search, true) ||
                    it.location.contains(search, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campaigns") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search...") },
                        singleLine = true,
                        modifier = Modifier.width(220.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (list.isEmpty()) {
                Text("No campaigns found.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list, key = { it.id }) { c ->
                        CampaignCard(c) {
                            navController.navigate(Screen.CampaignDetails.createRoute(c.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignCard(c: CampaignUi, onClick: () -> Unit) {
    val progress = if (c.goalAmount == 0) 0f else (c.raisedAmount.toFloat() / c.goalAmount).coerceIn(0f, 1f)
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(c.title, fontWeight = FontWeight.SemiBold)
            Text("${c.category} • ${c.location}", style = MaterialTheme.typography.bodySmall)
            Text("Raised: KSh ${c.raisedAmount} / ${c.goalAmount}", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            Text("Progress: ${(progress * 100).roundToInt()}% • Updated: ${c.lastUpdate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}