package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
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
fun ExpensesScreen(navController: NavController) {

    val expenses = remember {
        mutableStateListOf(
            ExpenseUi("Fuel & Transport", "Field Operations", 4500, "Today"),
            ExpenseUi("Stationery", "Admin", 1200, "Yesterday"),
            ExpenseUi("Medical Supplies", "Community Health Outreach", 18000, "2 days ago")
        )
    }

    var showAddExpense by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExpense = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
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
                Text("Recent expenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Track project spending and upload receipts later.", style = MaterialTheme.typography.bodyMedium)
            }

            items(expenses) { expense ->
                ExpenseCard(expense)
            }
        }
    }

    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { showAddExpense = false },
            onSave = { newExpense ->
                expenses.add(0, newExpense)
                showAddExpense = false
            }
        )
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseUi) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ReceiptLong, contentDescription = null)
            }
            Spacer(Modifier.height(8.dp))
            Text("Account/Project: ${expense.account}", style = MaterialTheme.typography.bodyMedium)
            Text("Amount: $${expense.amount}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${expense.date}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (ExpenseUi) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account / Project") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (numbers only)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toIntOrNull() ?: 0
                    if (category.isBlank() || account.isBlank()) return@TextButton
                    onSave(ExpenseUi(category.trim(), account.trim(), amount, "Just now"))
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class ExpenseUi(
    val category: String,
    val account: String,
    val amount: Int,
    val date: String
)

@Preview
@Composable
fun ExpensesScreenPreview() {
    ExpensesScreen(navController = rememberNavController())
}