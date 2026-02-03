package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(navController: NavController) {

    // ✅ UI-only sample data
    val summary = remember {
        FinanceSummaryUi(
            totalBudget = 2_000_000,
            totalSpent = 1_250_000,
            pendingApprovals = 7,
            donorFunds = 600_000
        )
    }

    val transactions = remember {
        listOf(
            TransactionUi("Invoice Payment", "Clean Water Initiative", -120_000, "Today"),
            TransactionUi("Grant Received", "LumiSphere Fund", 250_000, "Yesterday"),
            TransactionUi("Expense Reimbursement", "Field Operations", -18_500, "2 days ago"),
            TransactionUi("Supplier Payment", "School Renovation", -60_000, "1 week ago")
        )
    }

    val spentPercent =
        if (summary.totalBudget == 0) 0f else (summary.totalSpent.toFloat() / summary.totalBudget.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance & Reporting") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ✅ Summary KPI cards
            item {
                Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniFinanceCard("Total Budget", "$${summary.totalBudget}", Modifier.weight(1f))
                    MiniFinanceCard("Spent", "$${summary.totalSpent}", Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniFinanceCard("Pending Approvals", summary.pendingApprovals.toString(), Modifier.weight(1f))
                    MiniFinanceCard("Donor Funds", "$${summary.donorFunds}", Modifier.weight(1f))
                }
            }

            // ✅ Budget utilization block
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Budget Utilization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(progress = spentPercent, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Used ${(spentPercent * 100).roundToInt()}% of total budget.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { navController.navigate(Screen.Approvals.route) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.RequestQuote, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Approvals")
                            }

                            FilledTonalButton(
                                onClick = { navController.navigate(Screen.Expenses.route) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Expenses")
                            }
                        }
                    }
                }
            }

            // ✅ Reports section
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { navController.navigate(Screen.Reports.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Financial Summary")
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { /* later: export */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export CSV / PDF")
                        }
                    }
                }
            }

            // ✅ Transactions list
            item {
                Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(transactions) { tx ->
                TransactionCard(tx)
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MiniFinanceCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransactionCard(tx: TransactionUi) {
    val amountText = if (tx.amount >= 0) "+$${tx.amount}" else "-$${kotlin.math.abs(tx.amount)}"

    Card {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tx.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(amountText, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text("Project/Account: ${tx.account}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("Date: ${tx.date}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private data class FinanceSummaryUi(
    val totalBudget: Int,
    val totalSpent: Int,
    val pendingApprovals: Int,
    val donorFunds: Int
)

private data class TransactionUi(
    val title: String,
    val account: String,
    val amount: Int, // positive = incoming, negative = outgoing
    val date: String
)

@Preview
@Composable
fun FinanceScreenPreview() {
    FinanceScreen(navController = rememberNavController())
}