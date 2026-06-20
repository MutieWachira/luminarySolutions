package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

enum class DonationStep { CHOICE, PAYMENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    projectId: String,
    viewModel: DonationViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(DonationStep.CHOICE) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Make a Donation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentStep == DonationStep.PAYMENT) currentStep = DonationStep.CHOICE 
                        else navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentStep) {
                DonationStep.CHOICE -> DonationChoiceContent(
                    onGuestContinue = { currentStep = DonationStep.PAYMENT },
                    onSignUp = { /* Navigate to Sign Up flow if available */ currentStep = DonationStep.PAYMENT }
                )
                DonationStep.PAYMENT -> PaymentMethodContent(
                    viewModel = viewModel,
                    onSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun DonationChoiceContent(onGuestContinue: () -> Unit, onSignUp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your support matters", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Would you like to sign up to track your impact, or continue as a guest?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Sign Up & Donate")
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onGuestContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Continue as Guest")
        }
    }
}

@Composable
fun PaymentMethodContent(viewModel: DonationViewModel, onSuccess: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Enter Amount (KES)", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            prefix = { Text("KES ") }
        )

        Text("Select Payment Method", fontWeight = FontWeight.Bold)
        
        PaymentMethodItem(
            title = "M-Pesa",
            icon = Icons.Default.PhoneIphone,
            isSelected = selectedMethod == "M-Pesa",
            onClick = { selectedMethod = "M-Pesa" }
        )

        PaymentMethodItem(
            title = "Credit/Debit Card",
            icon = Icons.Default.CreditCard,
            isSelected = selectedMethod == "Card",
            onClick = { selectedMethod = "Card" }
        )

        Spacer(Modifier.weight(1f))

        if (uiState is DonationUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState is DonationUiState.Success) {
            LaunchedEffect(Unit) { onSuccess() }
        } else {
            if (uiState is DonationUiState.Error) {
                Text((uiState as DonationUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            
            Button(
                onClick = { viewModel.processDonation(amount.toIntOrNull() ?: 0, selectedMethod, null) },
                enabled = amount.isNotBlank() && selectedMethod.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Complete Donation")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}
