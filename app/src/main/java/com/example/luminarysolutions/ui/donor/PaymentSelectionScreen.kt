package com.example.luminarysolutions.ui.donor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.luminarysolutions.ui.donor.viewmodel.DonationUiState
import com.example.luminarysolutions.ui.donor.viewmodel.DonationViewModel

/**
 * Modern, Production-Ready Payment Selection Screen.
 * Features:
 * - Dynamic UI based on payment method
 * - Real-time validation and feedback
 * - Professional design with Material 3 components
 * - Secure handling of payment interactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionScreen(
    projectId: String? = null,
    donorId: String? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: DonationViewModel = hiltViewModel()
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

    val scrollState = rememberScrollState()

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
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Section
            ContributionAmountCard(
                amountText = amountText,
                onAmountChange = { 
                    val filtered = it.filter { char -> char.isDigit() || char == '.' }
                    if (filtered.count { char -> char == '.' } <= 1) {
                        amountText = filtered
                        viewModel.setAmount(filtered.toDoubleOrNull() ?: 0.0)
                    }
                }
            )

            Text(
                "Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            // Payment Methods Grid/Column
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernPaymentMethodCard(
                    name = "M-Pesa Express",
                    subtitle = "Instant STK Push",
                    icon = Icons.Default.PhoneIphone,
                    selected = selectedMethod == "Mpesa",
                    onClick = { selectedMethod = "Mpesa" }
                )

                AnimatedVisibility(visible = selectedMethod == "Mpesa") {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("M-Pesa Phone Number") },
                        placeholder = { Text("e.g. 2547XXXXXXXX") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(16.dp),
                        prefix = { Text("+", fontWeight = FontWeight.Bold) }
                    )
                }

                ModernPaymentMethodCard(
                    name = "Credit / Debit Card",
                    subtitle = "Visa, Mastercard, Amex",
                    icon = Icons.Default.CreditCard,
                    selected = selectedMethod == "Card",
                    onClick = { selectedMethod = "Card" }
                )

                AnimatedVisibility(visible = selectedMethod == "Card") {
                    CardDetailsForm(
                        cardNumber = cardNumber,
                        onCardNumberChange = { if (it.length <= 16) cardNumber = it },
                        expiryDate = expiryDate,
                        onExpiryChange = { if (it.length <= 5) expiryDate = it },
                        cvv = cvv,
                        onCvvChange = { if (it.length <= 3) cvv = it }
                    )
                }

                ModernPaymentMethodCard(
                    name = "Bank Transfer",
                    subtitle = "Direct deposit",
                    icon = Icons.Default.AccountBalance,
                    selected = selectedMethod == "Transfer",
                    onClick = { selectedMethod = "Transfer" }
                )
                
                AnimatedVisibility(visible = selectedMethod == "Transfer") {
                    OutlinedTextField(
                        value = bankAccountRef,
                        onValueChange = { bankAccountRef = it },
                        label = { Text("Transaction Reference") },
                        placeholder = { Text("Enter payment reference") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Status feedback
            PaymentStatusFeedback(uiState, viewModel)

            if (uiState is DonationUiState.Success) {
                LaunchedEffect(Unit) { onSuccess() }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
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
                    viewModel.processDonation(donorId, selectedMethod, projectId, details)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = uiState !is DonationUiState.Processing && 
                          uiState !is DonationUiState.AwaitingMpesaPrompt && 
                          amountText.isNotEmpty() &&
                          (selectedMethod != "Mpesa" || phoneNumber.isNotEmpty()),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                if (uiState is DonationUiState.Processing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Text(
                        if (uiState is DonationUiState.AwaitingMpesaPrompt) "Confirming..." else "Complete Donation", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ContributionAmountCard(amountText: String, onAmountChange: (String) -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Contribution Amount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium.copy(color = Color.LightGray)) },
                leadingIcon = { 
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                    ) {
                        Text(
                            "KES", 
                            fontWeight = FontWeight.Black, 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun ModernPaymentMethodCard(
    name: String, 
    subtitle: String,
    icon: ImageVector,
    selected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.2f)
        ),
        color = if (selected) Color(0xFF10B981).copy(alpha = 0.05f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (selected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            RadioButton(
                selected = selected, 
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
            )
        }
    }
}

@Composable
fun CardDetailsForm(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiryDate: String,
    onExpiryChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = cardNumber,
            onValueChange = onCardNumberChange,
            label = { Text("Card Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.CreditCard, null) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = onExpiryChange,
                label = { Text("MM/YY") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = onCvvChange,
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun PaymentStatusFeedback(uiState: DonationUiState, viewModel: DonationViewModel) {
    AnimatedContent(targetState = uiState, label = "StatusFeedback") { state ->
        when (state) {
            is DonationUiState.AwaitingMpesaPrompt -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { viewModel.checkMpesaStatus(state.checkoutRequestId) }) {
                            Text("Check Confirmation Status")
                        }
                    }
                }
            }
            is DonationUiState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            else -> {}
        }
    }
}
