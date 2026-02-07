package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrievancesScreen(navController: NavController) {

    val cases = remember {
        mutableStateListOf(
            CaseUi(UUID.randomUUID().toString(), "Anonymous", "Water point not working", "Open", "High", "Today"),
            CaseUi(UUID.randomUUID().toString(), "Community Member", "Unfair beneficiary selection", "Investigating", "Medium", "Yesterday"),
            CaseUi(UUID.randomUUID().toString(), "Anonymous", "Delayed supplies", "Resolved", "Low", "1 week ago")
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback & Grievances") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add case")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Text("Cases", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Track and resolve community feedback transparently.", style = MaterialTheme.typography.bodyMedium)
            }

            items(cases) { c ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(c.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Reporter: ${c.reporter}", style = MaterialTheme.typography.bodyMedium)
                        Text("Status: ${c.status}  •  Priority: ${c.priority}", style = MaterialTheme.typography.bodyMedium)
                        Text("Created: ${c.created}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCaseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newCase ->
                cases.add(0, newCase)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCaseDialog(
    onDismiss: () -> Unit,
    onSave: (CaseUi) -> Unit
) {
    var reporter by remember { mutableStateOf("Anonymous") }
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Open") }
    var priority by remember { mutableStateOf("Medium") }

    val statusOptions = listOf("Open", "Investigating", "Resolved")
    val priorityOptions = listOf("Low", "Medium", "High")

    var statusOpen by remember { mutableStateOf(false) }
    var priorityOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Case") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = reporter,
                    onValueChange = { reporter = it },
                    label = { Text("Reporter (Anonymous allowed)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Case title") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = statusOpen, onExpandedChange = { statusOpen = !statusOpen }) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = statusOpen, onDismissRequest = { statusOpen = false }) {
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { status = opt; statusOpen = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = priorityOpen, onExpandedChange = { priorityOpen = !priorityOpen }) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = priorityOpen, onDismissRequest = { priorityOpen = false }) {
                        priorityOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { priority = opt; priorityOpen = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) return@TextButton
                    onSave(
                        CaseUi(
                            id = UUID.randomUUID().toString(),
                            reporter = if (reporter.isBlank()) "Anonymous" else reporter.trim(),
                            title = title.trim(),
                            status = status,
                            priority = priority,
                            created = "Just now"
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class CaseUi(
    val id: String,
    val reporter: String,
    val title: String,
    val status: String,
    val priority: String,
    val created: String
)
