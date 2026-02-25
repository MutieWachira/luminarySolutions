package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerTaskDetailsScreen(
    navController: NavController,
    taskId: String,
    vm: VolunteerViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load("me") }

    val task = remember(vm.tasks, taskId) { vm.getTask(taskId) }

    if (task == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Task Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Task not found.")
            }
        }
        return
    }

    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUpdateDialog = true }) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Add update")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Location: ${task.location}")
            Text("Due: ${task.dueDate}")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Description", fontWeight = FontWeight.SemiBold)
                    Text(task.description)

                    Divider()

                    Text("Status", fontWeight = FontWeight.SemiBold)
                    StatusSelector(
                        current = task.status,
                        onChange = { vm.setStatus(task.id, it) }
                    )

                    Divider()

                    Text("Last Update", fontWeight = FontWeight.SemiBold)
                    Text(task.lastUpdate)
                }
            }
        }
    }

    if (showUpdateDialog) {
        AddTaskUpdateDialog(
            onDismiss = { showUpdateDialog = false },
            onSave = { note ->
                vm.addUpdate(task.id, note)
                showUpdateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(
    current: TaskStatus,
    onChange: (TaskStatus) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val options = listOf(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, TaskStatus.DONE)

    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = !open }) {
        OutlinedTextField(
            value = current.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) }
        )

        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name.replace("_", " ")) },
                    onClick = {
                        onChange(status)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddTaskUpdateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Update") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("What happened?") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (note.isBlank()) return@TextButton
                    onSave(note.trim())
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}