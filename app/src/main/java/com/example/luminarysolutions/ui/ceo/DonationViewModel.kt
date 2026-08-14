package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.FinanceRepository
import com.example.luminarysolutions.data.repository.PaymentRepository
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * ViewModel for processing donations.
 */
@HiltViewModel
class DonationViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Idle)
    val uiState: StateFlow<DonationUiState> = _uiState

    /**
     * Processes a donation.
     * @param amount The donation amount.
     * @param method The payment method (e.g., "M-Pesa", "Card").
     * @param donorId Optional ID of the registered user.
     * @param projectId Optional ID of the project being supported.
     * @param phoneNumber Optional phone number for M-Pesa.
     */
    fun processDonation(
        amount: Int, 
        method: String, 
        donorId: String?, 
        projectId: String? = null,
        phoneNumber: String? = null
    ) {
        if (amount <= 0) {
            _uiState.value = DonationUiState.Error("Invalid amount.")
            return
        }

        _uiState.value = DonationUiState.Loading

        if (method.equals("M-Pesa", ignoreCase = true) || method.equals("Mpesa", ignoreCase = true)) {
            if (phoneNumber.isNullOrBlank()) {
                _uiState.value = DonationUiState.Error("Phone number is required for M-Pesa.")
                return
            }

            viewModelScope.launch {
                val result = paymentRepository.initiatePayment(amount.toDouble(), phoneNumber, projectId)
                result.onSuccess { response ->
                    if (response.success && response.checkoutRequestId != null) {
                        _uiState.value = DonationUiState.AwaitingMpesaPrompt(
                            response.customerMessage ?: "Please check your phone for the M-Pesa prompt."
                        )
                        observeMpesaPayment(response.checkoutRequestId, donorId, projectId, phoneNumber)
                    } else {
                        _uiState.value = DonationUiState.Error(response.customerMessage ?: "M-Pesa initiation failed.")
                    }
                }.onFailure { error ->
                    _uiState.value = DonationUiState.Error(error.message ?: "M-Pesa service unavailable.")
                }
            }
        } else {
            viewModelScope.launch {
                financeRepository.addDonation(
                    donorId = donorId,
                    amount = amount.toDouble(),
                    method = method,
                    projectId = projectId
                ).onSuccess {
                    _uiState.value = DonationUiState.Success
                }.onFailure {
                    _uiState.value = DonationUiState.Error(it.message ?: "Transaction failed. Please try again.")
                }
            }
        }
    }

    private fun observeMpesaPayment(
        checkoutRequestId: String,
        donorId: String?,
        projectId: String?,
        phoneNumber: String
    ) {
        paymentRepository.observePayment(checkoutRequestId) { payment ->
            payment?.let {
                when (it.status) {
                    "Completed" -> {
                        viewModelScope.launch {
                            financeRepository.addDonation(
                                donorId = donorId,
                                amount = it.amount,
                                method = "M-Pesa",
                                projectId = projectId,
                                details = mapOf(
                                    "phone" to phoneNumber,
                                    "mpesaReceipt" to (it.mpesaReceiptNumber ?: "")
                                )
                            ).onSuccess {
                                _uiState.value = DonationUiState.Success
                            }.onFailure {
                                _uiState.value = DonationUiState.Error("Payment confirmed but failed to record.")
                            }
                        }
                    }
                    "Failed" -> {
                        _uiState.value = DonationUiState.Error("M-Pesa payment failed or cancelled.")
                    }
                    else -> { /* Keep Awaiting */ }
                }
            }
        }
    }

    /**
     * Processes a card donation with specific card details.
     */
    fun processCardDonation(
        amount: Int,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        stripe: Stripe,
        donorId: String?,
        projectId: String? = null
    ) {
        if (amount <= 0) {
            _uiState.value = DonationUiState.Error("Invalid amount.")
            return
        }

        _uiState.value = DonationUiState.Loading

        viewModelScope.launch {
            // 1. Parse Expiry
            val parts = expiryDate.split("/")
            if (parts.size != 2) {
                _uiState.value = DonationUiState.Error("Invalid expiry date format (use MM/YY)")
                return@launch
            }
            val month = parts[0].toIntOrNull() ?: 0
            val year = parts[1].toIntOrNull() ?: 0

            // 2. Create PaymentMethod via Stripe SDK (PCI Compliant)
            val cardParams = CardParams(cardNumber, month, year, cvv)
            val params = PaymentMethodCreateParams.createCard(cardParams)

            val paymentMethodId = createPaymentMethod(stripe, params)
            if (paymentMethodId == null) {
                _uiState.value = DonationUiState.Error("Failed to tokenize card information")
                return@launch
            }

            // 3. Send PaymentMethodId to our backend
            val result = paymentRepository.processCardPayment(
                amount.toDouble(),
                paymentMethodId,
                reference = projectId ?: "General Donation"
            )

            result.onSuccess { success ->
                if (success) {
                    viewModelScope.launch {
                        financeRepository.addDonation(
                            donorId = donorId,
                            amount = amount.toDouble(),
                            method = "Card",
                            projectId = projectId
                        ).onSuccess {
                            _uiState.value = DonationUiState.Success
                        }.onFailure {
                            _uiState.value = DonationUiState.Error("Payment successful but failed to record. Contact support.")
                        }
                    }
                } else {
                    _uiState.value = DonationUiState.Error("Card payment failed. Please check your details.")
                }
            }.onFailure { error ->
                _uiState.value = DonationUiState.Error(error.message ?: "An unexpected error occurred.")
            }
        }
    }

    private suspend fun createPaymentMethod(stripe: Stripe, params: PaymentMethodCreateParams): String? =
        suspendCancellableCoroutine { continuation ->
            stripe.createPaymentMethod(params, callback = object : com.stripe.android.ApiResultCallback<com.stripe.android.model.PaymentMethod> {
                override fun onSuccess(result: com.stripe.android.model.PaymentMethod) {
                    continuation.resume(result.id)
                }
                override fun onError(e: Exception) {
                    continuation.resume(null)
                }
            })
        }

    fun resetState() {
        _uiState.value = DonationUiState.Idle
    }
}

sealed class DonationUiState {
    object Idle : DonationUiState()
    object Loading : DonationUiState()
    object Success : DonationUiState()
    data class AwaitingMpesaPrompt(val message: String) : DonationUiState()
    data class Error(val message: String) : DonationUiState()
}
