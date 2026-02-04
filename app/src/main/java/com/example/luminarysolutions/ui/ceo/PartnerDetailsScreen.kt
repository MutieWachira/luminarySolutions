package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDetailsScreen(
    navController: NavController,
    partnerId: String
) {
    // UI-only: pretend we loaded this partner by partnerId
    val profile = remember(partnerId) {
        PartnerProfileUi(
            id = partnerId,
            name = "Selected Partner/Donor",
            type = "Donor",
            status = "Active",
            primaryContact = "contact@email.com",
            phone = "+254 700 000 000",
            notes = "Profile loaded using partnerId: $partnerId"
        )
    }



    val engagements = remember {
        mutableStateListOf(
            EngagementUi("Email", "Sent Q1 Impact report", "Today"),
            EngagementUi("Meeting", "Grant compliance check-in", "Yesterday"),
            EngagementUi("Call", "Discussed next disbursement", "1 week ago")
        )
    }

    val agreements = remember { mutableStateListOf<String>() }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            agreements.add(0, uri.toString())
        }
    }


    var showAddEngagement by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddEngagement = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add engagement")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(profile.type) })
                    AssistChip(onClick = {}, label = { Text(profile.status) })
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Text(profile.primaryContact)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Text(profile.phone)
                        }

                        Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(profile.notes)
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Agreements / MoUs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = { pickFileLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upload Agreement (Placeholder)")
                        }

                        if (agreements.isEmpty()) {
                            Text("No files uploaded yet.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            agreements.take(3).forEach { file ->
                                Text("• $file", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }


            item {
                Text("Engagement History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(engagements) { e ->
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(e.type, fontWeight = FontWeight.SemiBold)
                            Text(e.summary, style = MaterialTheme.typography.bodyMedium)
                            Text(e.date, style = MaterialTheme.typography.labelMedium)
                        }
                        Icon(Icons.Default.Event, contentDescription = null)
                    }
                }
            }
        }
    }

    if (showAddEngagement) {
        AddEngagementDialog(
            onDismiss = { showAddEngagement = false },
            onSave = { newEngagement ->
                engagements.add(0, newEngagement)
                showAddEngagement = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEngagementDialog(
    onDismiss: () -> Unit,
    onSave: (EngagementUi) -> Unit
) {
    val typeOptions = listOf("Email", "Meeting", "Call", "Proposal")
    var type by remember { mutableStateOf("Email") }
    var expanded by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Engagement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { type = option; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (summary.isBlank()) return@TextButton
                    onSave(EngagementUi(type, summary.trim(), "Just now"))
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class PartnerProfileUi(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val primaryContact: String,
    val phone: String,
    val notes: String
)

data class EngagementUi(
    val type: String,
    val summary: String,
    val date: String
)

@Preview
@Composable
fun PartnerDetailsScreenPreview() {
    PartnerDetailsScreen(navController = rememberNavController(), partnerId = "123")
}

//
