package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDashboardScreen(
    navController: NavController,
    vm: VolunteerViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Volunteer Dashboard") }) }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text("Today Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickStat("Tasks", vm.tasks.count().toString(), Modifier.weight(1f))
                QuickStat("Upcoming Events", vm.events.count().toString(), Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.navigate(Screen.VolunteerTasks.route) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("My Tasks")
                }

                Button(
                    onClick = { navController.navigate(Screen.VolunteerEvents.route) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("My Events")
                }
            }

            Text("Recent Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.tasks.take(3)) { t ->
                    Card(onClick = { navController.navigate(Screen.VolunteerTaskDetails.createRoute(t.id)) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(t.title, fontWeight = FontWeight.SemiBold)
                            Text("Status: ${t.status} • Due: ${t.dueDate}", style = MaterialTheme.typography.bodySmall)
                            Text("Last update: ${t.lastUpdate}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStat(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}