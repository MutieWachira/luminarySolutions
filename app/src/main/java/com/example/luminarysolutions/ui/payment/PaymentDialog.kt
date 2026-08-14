package com.example.luminarysolutions.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stripe.android.Stripe

@Composable
fun PaymentDialog(
    onDismiss: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // REPLACE WITH YOUR ACTUAL STRIPE PUBLISHABLE KEY
    val stripe = remember { Stripe(context, "pk_test_51RqYeTAmgfWuLbTcVTAeDQrhkYc9Q5a4dzY0wxOkKKbB9aomDoLYAOZjTDaP7bnbSvzMH61Z80vHwPQMLjTQUrXB00HOtUTC5v") }
    
    var selectedMethod by remember { mutableStateOf("M-Pesa") }
    var amount by remember { mutableStateOf("") }
    
    // M-Pesa specific
    var phoneNumber by remember { mutableStateOf("") }
    
    // Card specific
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(Modifier.width(8.dp))
                Text("Make Payment", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState is PaymentUiState.Idle) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        label = { Text("Amount (KSh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Choose Method", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedMethod == "M-Pesa",
                            onClick = { selectedMethod = "M-Pesa" },
                            label = { Text("M-Pesa") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMethod == "Card",
                            onClick = { selectedMethod = "Card" },
                            label = { Text("Card") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedMethod == "M-Pesa") {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("2547XXXXXXXX") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 16) cardNumber = it },
                            label = { Text("Card Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { if (it.length <= 5) expiryDate = it },
                                label = { Text("Expiry") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { if (it.length <= 3) cvv = it },
                                label = { Text("CVV") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    PaymentStatusContent(
                        uiState = uiState,
                        onCheckStatus = {
                            (uiState as? PaymentUiState.Processing)?.let {
                                viewModel.checkStatusManual(it.checkoutRequestId)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            if (uiState is PaymentUiState.Idle) {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            if (selectedMethod == "M-Pesa") {
                                if (phoneNumber.isNotEmpty()) viewModel.initiatePayment(amt, phoneNumber)
                            } else {
                                if (cardNumber.length >= 16 && expiryDate.length >= 5 && cvv.length >= 3) {
                                    viewModel.initiateCardPayment(amt, cardNumber, expiryDate, cvv, stripe)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Pay Now")
                }
            } else if (uiState is PaymentUiState.Success || uiState is PaymentUiState.Error) {
                Button(onClick = {
                    if (uiState is PaymentUiState.Success) onDismiss()
                    viewModel.resetState()
                }) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (uiState is PaymentUiState.Idle) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun PaymentStatusContent(uiState: PaymentUiState, onCheckStatus: () -> Unit = {}) {
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        when (uiState) {
            is PaymentUiState.Loading -> CircularProgressIndicator()
            is PaymentUiState.Processing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                    Spacer(Modifier.height(16.dp))
                    Text("Processing... Please wait", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onCheckStatus) {
                        Text("Already Paid? Check Status")
                    }
                }
            }
            is PaymentUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ Payment Successful!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    if (uiState.payment.mpesaReceiptNumber != null) {
                        Text("Receipt: ${uiState.payment.mpesaReceiptNumber}")
                    }
                }
            }
            is PaymentUiState.Error -> {
                Text(uiState.message, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}

@Composable
fun MpesaPaymentDialog(
    onDismiss: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    // Keep this for backward compatibility if needed, or just redirect to Unified
    PaymentDialog(onDismiss, viewModel)
}
