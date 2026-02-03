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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersDonorsScreen(navController: NavController) {

    val allContacts = remember {
        mutableStateListOf(
            PartnerUi("LumiSphere Fund", "Donor", "Active", "KES 3,200,000", "Last contact: Today"),
            PartnerUi("UNICEF Kenya", "Partner", "Active", "MoU signed", "Last contact: Yesterday"),
            PartnerUi("Safaricom Foundation", "Donor", "Active", "KES 1,500,000", "Last contact: 2 days ago"),
            PartnerUi("County Government", "Partner", "Pending", "Proposal submitted", "Last contact: 1 week ago")
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ContactFilter.ALL) }

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
            FloatingActionButton(onClick = { /* later: add partner/donor */ }) {
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

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search partners or donors") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filters
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

            Spacer(Modifier.height(4.dp))

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
                            // UI-only navigation for now
                            navController.navigate(Screen.PartnerDetails.route)
                        }
                    )
                }
            }
        }
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

private enum class ContactFilter { ALL, PARTNERS, DONORS }

private data class PartnerUi(
    val name: String,
    val type: String, // "Partner" or "Donor"
    val status: String, // Active/Pending/Inactive
    val valueOrNote: String, // e.g., funds or MoU status
    val lastContact: String
)
