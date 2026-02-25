package com.example.luminarysolutions.ui.volunteer

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
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.models.VolunteerTaskUi
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerTasksScreen(
    navController: NavController,
    vm: VolunteerViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }

    val filtered = remember(vm.tasks, search, filter) {
        vm.tasks
            .filter {
                when (filter) {
                    TaskFilter.ALL -> true
                    TaskFilter.ASSIGNED -> it.status == TaskStatus.ASSIGNED
                    TaskFilter.IN_PROGRESS -> it.status == TaskStatus.IN_PROGRESS
                    TaskFilter.DONE -> it.status == TaskStatus.DONE
                }
            }
            .filter { it.title.contains(search, true) || it.location.contains(search, true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
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
                        placeholder = { Text("Search tasks...") },
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

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == TaskFilter.ALL, onClick = { filter = TaskFilter.ALL }, label = { Text("All") })
                FilterChip(selected = filter == TaskFilter.ASSIGNED, onClick = { filter = TaskFilter.ASSIGNED }, label = { Text("Assigned") })
                FilterChip(selected = filter == TaskFilter.IN_PROGRESS, onClick = { filter = TaskFilter.IN_PROGRESS }, label = { Text("In Progress") })
                FilterChip(selected = filter == TaskFilter.DONE, onClick = { filter = TaskFilter.DONE }, label = { Text("Done") })
            }

            // KPI Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniStat("Total", vm.tasks.size.toString(), Modifier.weight(1f))
                MiniStat("Assigned", vm.tasks.count { it.status == TaskStatus.ASSIGNED }.toString(), Modifier.weight(1f))
                MiniStat("In Progress", vm.tasks.count { it.status == TaskStatus.IN_PROGRESS }.toString(), Modifier.weight(1f))
                MiniStat("Done", vm.tasks.count { it.status == TaskStatus.DONE }.toString(), Modifier.weight(1f))
            }

            // Table-like list
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // Header row (table feel)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Task", fontWeight = FontWeight.Bold)
                        Text("Status", fontWeight = FontWeight.Bold)
                    }

                    Divider()

                    if (filtered.isEmpty()) {
                        Text("No tasks match your filter/search.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filtered, key = { it.id }) { task ->
                                TaskRow(task = task) {
                                    navController.navigate(Screen.VolunteerTaskDetails.createRoute(task.id))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: VolunteerTaskUi, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(task.title, fontWeight = FontWeight.SemiBold)
                Text("Due: ${task.dueDate} • ${task.location}", style = MaterialTheme.typography.bodySmall)
                Text("Last: ${task.lastUpdate}", style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(task.status)
        }
    }
}

@Composable
private fun StatusPill(status: TaskStatus) {
    val (label, colors) = when (status) {
        TaskStatus.ASSIGNED -> "Assigned" to AssistChipDefaults.assistChipColors()
        TaskStatus.IN_PROGRESS -> "In Progress" to AssistChipDefaults.assistChipColors()
        TaskStatus.DONE -> "Done" to AssistChipDefaults.assistChipColors()
    }

    AssistChip(onClick = {}, label = { Text(label) }, colors = colors)
}

@Composable
private fun MiniStat(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private enum class TaskFilter { ALL, ASSIGNED, IN_PROGRESS, DONE }