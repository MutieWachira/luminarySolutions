package com.example.luminarysolutions.ui.donor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.FinanceRepository
import com.example.luminarysolutions.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _amount = MutableStateFlow(0.0)
    val amount = _amount.asStateFlow()

    fun setAmount(value: Double) {
        _amount.value = value
    }

    fun processDonation(donorId: String?, method: String, projectId: String? = null, details: Map<String, String> = emptyMap()) {
        if (_amount.value <= 0) {
            _uiState.value = DonationUiState.Error("Please enter a valid amount")
            return
        }

        _uiState.value = DonationUiState.Processing

        // Handle M-Pesa specifically to trigger STK Push
        if (method.equals("Mpesa", ignoreCase = true) || method.equals("M-Pesa", ignoreCase = true)) {
            val phoneNumber = details["phone"] ?: ""
            if (phoneNumber.isBlank()) {
                _uiState.value = DonationUiState.Error("Phone number is required for M-Pesa")
                return
            }

            viewModelScope.launch {
                try {
                    val result = paymentRepository.initiatePayment(_amount.value, phoneNumber)
                    result.onSuccess { response ->
                        android.util.Log.d("DonationViewModel", "M-Pesa response: $response")
                        if (response.success && response.checkoutRequestId != null) {
                            // Request accepted by Safaricom, now we wait for the user to enter PIN
                            _uiState.value = DonationUiState.AwaitingMpesaPrompt(
                                response.customerMessage ?: "Please check your phone for the M-Pesa prompt",
                                response.checkoutRequestId
                            )
                            observeMpesaPayment(response.checkoutRequestId, donorId, projectId, details)
                        } else {
                            _uiState.value = DonationUiState.Error(response.customerMessage ?: "M-Pesa initiation failed")
                        }
                    }.onFailure { error ->
                        android.util.Log.e("DonationViewModel", "M-Pesa initiation failed", error)
                        _uiState.value = DonationUiState.Error(error.message ?: "M-Pesa service is currently unavailable. Please try again later.")
                    }
                } catch (e: Exception) {
                    _uiState.value = DonationUiState.Error("An unexpected error occurred: ${e.localizedMessage}")
                }
            }
        } else {
            // Standard flow for other methods (Card, Transfer, etc.)
            viewModelScope.launch {
                repository.addDonation(donorId, _amount.value, method, projectId = projectId, details = details).onSuccess {
                    _uiState.value = DonationUiState.Success
                }.onFailure { error ->
                    _uiState.value = DonationUiState.Error(error.localizedMessage ?: "Payment failed. Please try again.")
                }
            }
        }
    }

    fun checkMpesaStatus(checkoutRequestId: String) {
        viewModelScope.launch {
            paymentRepository.queryPaymentStatus(checkoutRequestId).onSuccess { data ->
                if (data["ResultCode"]?.toString() != "0" && data["ResultCode"] != null) {
                    _uiState.value = DonationUiState.Error(data["ResultDesc"]?.toString() ?: "Payment failed")
                }
            }
        }
    }

    private fun observeMpesaPayment(checkoutRequestId: String, donorId: String?, projectId: String?, details: Map<String, String>) {
        paymentRepository.observePayment(checkoutRequestId) { payment ->
            payment?.let {
                when (it.status) {
                    "Completed" -> {
                        // Once M-Pesa is completed, record it in the main donations collection
                        viewModelScope.launch {
                            repository.addDonation(
                                donorId, 
                                _amount.value,
                                "M-Pesa", 
                                projectId = projectId,
                                details = details + ("mpesaReceipt" to (it.mpesaReceiptNumber ?: ""))
                            ).onSuccess {
                                _uiState.value = DonationUiState.Success
                            }.onFailure { error ->
                                _uiState.value = DonationUiState.Error(error.localizedMessage ?: "Payment confirmed but failed to record. Please contact support with receipt: ${it.mpesaReceiptNumber}")
                            }
                        }
                    }
                    "Failed" -> {
                        _uiState.value = DonationUiState.Error("M-Pesa payment failed or was cancelled by user.")
                    }
                    else -> {
                        // Keep the UI in AwaitingMpesaPrompt state if it's still pending
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = DonationUiState.Idle
        _amount.value = 0.0
    }
}

sealed class DonationUiState {
    object Idle : DonationUiState()
    object Processing : DonationUiState()
    object Success : DonationUiState()
    data class AwaitingMpesaPrompt(val message: String, val checkoutRequestId: String) : DonationUiState()
    data class Error(val message: String) : DonationUiState()
}
