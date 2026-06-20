package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luminarysolutions.ui.donor.viewmodel.DonationUiState
import com.example.luminarysolutions.ui.donor.viewmodel.DonationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionScreen(
    projectId: String? = null,
    donorId: String? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: DonationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Mpesa") }
    
    // Payment detail states
    var phoneNumber by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var bankAccountRef by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Complete Donation", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Contribution Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            amountText = filtered
                            viewModel.setAmount(filtered.toIntOrNull() ?: 0)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("0.00") },
                        leadingIcon = { Text("KSh", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)

            // Modern Payment Options
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModernPaymentMethodCard(
                    name = "M-Pesa Mobile Money",
                    icon = Icons.Default.PhoneAndroid,
                    selected = selectedMethod == "Mpesa",
                    onClick = { selectedMethod = "Mpesa" }
                )

                if (selectedMethod == "Mpesa") {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("M-Pesa Phone Number") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ModernPaymentMethodCard(
                    name = "Credit or Debit Card",
                    icon = Icons.Default.CreditCard,
                    selected = selectedMethod == "Card",
                    onClick = { selectedMethod = "Card" }
                )

                if (selectedMethod == "Card") {
                    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("MM/YY") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it },
                                label = { Text("CVV") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                ModernPaymentMethodCard(
                    name = "Direct Bank Transfer",
                    icon = Icons.Default.AccountBalance,
                    selected = selectedMethod == "Transfer",
                    onClick = { selectedMethod = "Transfer" }
                )
                
                if (selectedMethod == "Transfer") {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            "Please use the reference below in your transfer:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = bankAccountRef,
                            onValueChange = { bankAccountRef = it },
                            label = { Text("Transaction Reference") },
                            placeholder = { Text("e.g. TRN-123456") },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (uiState is DonationUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (uiState as DonationUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (uiState is DonationUiState.Success) {
                LaunchedEffect(Unit) { onSuccess() }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    // Pack details into a map for the ViewModel
                    val details = mutableMapOf<String, String>()
                    when(selectedMethod) {
                        "Mpesa" -> details["phone"] = phoneNumber
                        "Card" -> {
                            details["card"] = cardNumber
                            details["expiry"] = expiryDate
                            details["cvv"] = cvv
                        }
                        "Transfer" -> details["reference"] = bankAccountRef
                    }
                    viewModel.processDonation(donorId, selectedMethod, details)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = uiState !is DonationUiState.Processing && amountText.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                if (uiState is DonationUiState.Processing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Confirm Donation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ModernPaymentMethodCard(
    name: String, 
    icon: ImageVector,
    selected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.5f)
        ),
        color = if (selected) Color(0xFF10B981).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color(0xFF10B981) else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                name, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selected, 
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
            )
        }
    }
}
