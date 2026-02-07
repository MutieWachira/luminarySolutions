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
fun BeneficiariesScreen(navController: NavController) {

    val beneficiaries = remember {
        mutableStateListOf(
            BeneficiaryUi(UUID.randomUUID().toString(), "Amina K.", "Mombasa", "Eligible", "Today"),
            BeneficiaryUi(UUID.randomUUID().toString(), "John M.", "Kilifi", "Pending", "Yesterday"),
            BeneficiaryUi(UUID.randomUUID().toString(), "Fatma S.", "Kwale", "Eligible", "2 days ago")
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beneficiaries") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add beneficiary")
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
                Text("Registered beneficiaries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("UI-only. Later we connect to Firestore.", style = MaterialTheme.typography.bodyMedium)
            }

            items(beneficiaries) { b ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(b.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Location: ${b.location}", style = MaterialTheme.typography.bodyMedium)
                        Text("Eligibility: ${b.eligibility}", style = MaterialTheme.typography.bodyMedium)
                        Text("Added: ${b.added}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBeneficiaryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newB ->
                beneficiaries.add(0, newB)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBeneficiaryDialog(
    onDismiss: () -> Unit,
    onSave: (BeneficiaryUi) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf("Pending") }
    var menuOpen by remember { mutableStateOf(false) }

    val options = listOf("Eligible", "Pending", "Not Eligible")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Beneficiary") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (County/Town)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = !menuOpen }) {
                    OutlinedTextField(
                        value = eligibility,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Eligibility") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = { eligibility = opt; menuOpen = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || location.isBlank()) return@TextButton
                    onSave(
                        BeneficiaryUi(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            location = location.trim(),
                            eligibility = eligibility,
                            added = "Just now"
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class BeneficiaryUi(
    val id: String,
    val name: String,
    val location: String,
    val eligibility: String,
    val added: String
)
