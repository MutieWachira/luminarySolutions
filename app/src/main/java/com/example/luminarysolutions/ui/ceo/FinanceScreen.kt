package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FinanceScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var totalBudget by remember { mutableStateOf(0) }
    var utilizedBudget by remember { mutableStateOf(0) }

    // Listen to finance summary document
    LaunchedEffect(true) {
        db.collection("finance").document("summary")
            .addSnapshotListener { doc, _ ->
                totalBudget = doc?.getLong("total_budget")?.toInt() ?: 0
                utilizedBudget = doc?.getLong("utilized_budget")?.toInt() ?: 0
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Finance Overview", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Budget: $${totalBudget}", style = MaterialTheme.typography.titleMedium)
                Text("Budget Utilized: $${utilizedBudget}", style = MaterialTheme.typography.bodyLarge)
                val impact = if (totalBudget != 0) (utilizedBudget * 100 / totalBudget) else 0
                Text("Impact Score: $impact%", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
