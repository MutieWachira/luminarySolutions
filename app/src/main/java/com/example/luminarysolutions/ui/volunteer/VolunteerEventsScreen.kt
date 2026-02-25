package com.example.luminarysolutions.ui.volunteer

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
import com.example.luminarysolutions.ui.volunteer.models.VolunteerEventUi
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerEventsScreen(
    navController: NavController,
    vm: VolunteerViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Events") },
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

            Text("Upcoming Events", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Event", fontWeight = FontWeight.Bold)
                        Text("Time", fontWeight = FontWeight.Bold)
                    }

                    Divider()

                    if (vm.events.isEmpty()) {
                        Text("No upcoming events.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(vm.events, key = { it.id }) { e ->
                                EventRow(e)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(e: VolunteerEventUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(e.name, fontWeight = FontWeight.SemiBold)
                Text(e.venue, style = MaterialTheme.typography.bodySmall)
                Text("Notes: ${e.notes}", style = MaterialTheme.typography.bodySmall)
            }
            Text(e.date, fontWeight = FontWeight.SemiBold)
        }
    }
}