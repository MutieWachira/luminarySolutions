package com.example.luminarysolutions.ui.ceo

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(navController: NavController) {

    val approvals = remember {
        mutableStateListOf(
            ApprovalUi("Budget Increase", "Clean Water Initiative", 150000, "High", "Today"),
            ApprovalUi("Expense Approval", "Field Operations", 18500, "Medium", "Yesterday"),
            ApprovalUi("Grant Disbursement", "LumiSphere Program", 200000, "High", "2 days ago"),
            ApprovalUi("Vendor Payment", "School Renovation", 60000, "Low", "1 week ago")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approvals") },
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
                Text(
                    "Pending approvals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Approve or reject finance actions that affect budgets, payments, and donor funds.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(approvals) { approval ->
                ApprovalCard(
                    approval = approval,
                    onApprove = { approvals.remove(approval) },
                    onReject = { approvals.remove(approval) }
                )
            }

            if (approvals.isEmpty()) {
                item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("No pending approvals 🎉", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text("All requests have been processed.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalCard(
    approval: ApprovalUi,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(approval.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, label = { Text(approval.priority) })
            }

            Spacer(Modifier.height(8.dp))

            Text("Account/Project: ${approval.account}", style = MaterialTheme.typography.bodyMedium)
            Text("Amount: $${approval.amount}", style = MaterialTheme.typography.bodyMedium)
            Text("Requested: ${approval.date}", style = MaterialTheme.typography.labelMedium)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text("Reject")
                }
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                    Text("Approve")
                }
            }
        }
    }
}

private data class ApprovalUi(
    val type: String,
    val account: String,
    val amount: Int,
    val priority: String,
    val date: String
)

@Preview
@Composable
fun ApprovalsScreenPreview() {
    ApprovalsScreen(navController = rememberNavController())
}