package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.navigation.Screen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersDonorsScreen(navController: NavController) {

    val allContacts = remember {
        mutableStateListOf(
            PartnerUi(UUID.randomUUID().toString(),"LumiSphere Fund","Donor","Active","KES 3,200,000","Last contact: Today"),
            PartnerUi(UUID.randomUUID().toString(),"UNICEF Kenya","Partner","Active","MoU signed","Last contact: Yesterday"),
            PartnerUi(UUID.randomUUID().toString(),"Safaricom Foundation","Donor","Active","KES 1,500,000","Last contact: 2 days ago"),
            PartnerUi(UUID.randomUUID().toString(),"County Government","Partner","Pending","Proposal submitted","Last contact: 1 week ago")
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ContactFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(query, selectedFilter, allContacts) {
        allContacts
            .filter { it.name.contains(query, ignoreCase = true) }
            .filter {
                when (selectedFilter) {
                    ContactFilter.ALL -> true
                    ContactFilter.PARTNERS -> it.type == "Partner"
                    ContactFilter.DONORS -> it.type == "Donor"
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partners & Donors") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search partners or donors") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == ContactFilter.ALL,
                    onClick = { selectedFilter = ContactFilter.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == ContactFilter.PARTNERS,
                    onClick = { selectedFilter = ContactFilter.PARTNERS },
                    label = { Text("Partners") }
                )
                FilterChip(
                    selected = selectedFilter == ContactFilter.DONORS,
                    onClick = { selectedFilter = ContactFilter.DONORS },
                    label = { Text("Donors") }
                )
            }

            Text(
                "Contacts (${filtered.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { partner ->
                    PartnerCard(
                        partner = partner,
                        onClick = {
                            navController.navigate(Screen.PartnerDetails.createRoute(partner.id))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPartnerDonorDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newContact ->
                allContacts.add(0, newContact)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PartnerCard(partner: PartnerUi, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(partner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, label = { Text(partner.type) })
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Status: ${partner.status}", style = MaterialTheme.typography.bodyMedium)
                Text(partner.valueOrNote, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }

            Text(partner.lastContact, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPartnerDonorDialog(
    onDismiss: () -> Unit,
    onSave: (PartnerUi) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var valueOrNote by remember { mutableStateOf("") }

    val typeOptions = listOf("Partner", "Donor")
    var type by remember { mutableStateOf("Partner") }
    var typeMenuOpen by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "Pending", "Inactive")
    var status by remember { mutableStateOf("Active") }
    var statusMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Partner / Donor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeMenuOpen,
                    onExpandedChange = { typeMenuOpen = !typeMenuOpen }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeMenuOpen, onDismissRequest = { typeMenuOpen = false }) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { type = option; typeMenuOpen = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = statusMenuOpen,
                    onExpandedChange = { statusMenuOpen = !statusMenuOpen }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = statusMenuOpen, onDismissRequest = { statusMenuOpen = false }) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { status = option; statusMenuOpen = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = valueOrNote,
                    onValueChange = { valueOrNote = it },
                    label = { Text("Value / Note (Funds, MoU, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) return@TextButton
                    onSave(
                        PartnerUi(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = type,
                            status = status,
                            valueOrNote = if (valueOrNote.isBlank()) "—" else valueOrNote.trim(),
                            lastContact = "Last contact: Just now"
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private enum class ContactFilter { ALL, PARTNERS, DONORS }

data class PartnerUi(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val valueOrNote: String,
    val lastContact: String
)



@Preview
@Composable
fun PartnersDonorsScreenPreview() {
    PartnersDonorsScreen(navController = rememberNavController())
}