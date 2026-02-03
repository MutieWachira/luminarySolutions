package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDetailsScreen(navController: NavController) {

    // UI-only sample profile + engagements
    val profile = remember {
        PartnerProfileUi(
            name = "LumiSphere Fund",
            type = "Donor",
            status = "Active",
            primaryContact = "finance@lumisphere.org",
            phone = "+254 700 000 000",
            notes = "Key strategic funder for multi-year programs."
        )
    }

    val engagements = remember {
        listOf(
            EngagementUi("Email", "Sent Q1 Impact report", "Today"),
            EngagementUi("Meeting", "Grant compliance check-in", "Yesterday"),
            EngagementUi("Call", "Discussed next disbursement", "1 week ago")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Text("Engagement History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(engagements) { e ->
                Card {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
}

private data class PartnerProfileUi(
    val name: String,
    val type: String,
    val status: String,
    val primaryContact: String,
    val phone: String,
    val notes: String
)

private data class EngagementUi(
    val type: String, // Email/Meeting/Call
    val summary: String,
    val date: String
)
