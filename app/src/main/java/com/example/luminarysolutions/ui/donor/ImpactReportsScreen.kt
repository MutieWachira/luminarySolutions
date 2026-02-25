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
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactReportsScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impact Reports") },
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
            if (vm.reports.isEmpty()) {
                Text("No reports published yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.reports, key = { it.id }) { r ->
                        ReportCard(r)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(r: ImpactReportUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.title, fontWeight = FontWeight.SemiBold)
            Text("Period: ${r.period}", style = MaterialTheme.typography.bodySmall)
            Text("Published: ${r.publishedOn}", style = MaterialTheme.typography.bodySmall)
            Divider()
            Text(r.summary)
        }
    }
}