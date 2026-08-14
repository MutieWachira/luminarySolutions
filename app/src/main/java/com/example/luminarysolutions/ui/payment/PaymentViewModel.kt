package com.example.luminarysolutions.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Payment
import com.example.luminarysolutions.data.repository.PaymentRepository
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Processing(val checkoutRequestId: String) : PaymentUiState()
    data class Success(val payment: Payment) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun initiatePayment(amount: Double, phoneNumber: String, reference: String? = null) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            val result = repository.initiatePayment(amount, phoneNumber, reference)
            
            result.onSuccess { response ->
                if (response.success && response.checkoutRequestId != null) {
                    _uiState.value = PaymentUiState.Processing(response.checkoutRequestId)
                    observePaymentStatus(response.checkoutRequestId)
                } else {
                    _uiState.value = PaymentUiState.Error(response.customerMessage ?: "Initiation failed")
                }
            }.onFailure { error ->
                _uiState.value = PaymentUiState.Error(error.message ?: "An unexpected error occurred")
            }
        }
    }

    fun initiateCardPayment(
        amount: Double,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        stripe: Stripe,
        reference: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading

            // 1. Parse Expiry
            val parts = expiryDate.split("/")
            if (parts.size != 2) {
                _uiState.value = PaymentUiState.Error("Invalid expiry date format")
                return@launch
            }
            val month = parts[0].toIntOrNull() ?: 0
            val year = parts[1].toIntOrNull() ?: 0

            // 2. Create PaymentMethod via Stripe SDK (PCI Compliant)
            val cardParams = CardParams(cardNumber, month, year, cvv)
            val params = PaymentMethodCreateParams.createCard(cardParams)

            val paymentMethodId = createPaymentMethod(stripe, params)
            if (paymentMethodId == null) {
                _uiState.value = PaymentUiState.Error("Failed to tokenize card information")
                return@launch
            }

            // 3. Send PaymentMethodId to our backend
            val result = repository.processCardPayment(amount, paymentMethodId, reference)
            
            result.onSuccess { success ->
                if (success) {
                    _uiState.value = PaymentUiState.Success(Payment(
                        amount = amount,
                        status = "Completed",
                        reference = reference ?: "Card Payment"
                    ))
                } else {
                    _uiState.value = PaymentUiState.Error("Card payment failed. Please check your details.")
                }
            }.onFailure { error ->
                _uiState.value = PaymentUiState.Error(error.message ?: "An unexpected error occurred")
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

    fun checkStatusManual(checkoutRequestId: String) {
        viewModelScope.launch {
            val result = repository.queryPaymentStatus(checkoutRequestId)
            result.onSuccess { data ->
                val resultCode = data["ResultCode"]?.toString()
                if (resultCode == "0") {
                    // Success will be picked up by the Firestore observer
                } else if (resultCode != null) {
                    _uiState.value = PaymentUiState.Error(data["ResultDesc"]?.toString() ?: "Payment failed")
                }
            }
        }
    }

    private fun observePaymentStatus(checkoutRequestId: String) {
        repository.observePayment(checkoutRequestId) { payment ->
            payment?.let {
                when (it.status) {
                    "Completed" -> _uiState.value = PaymentUiState.Success(it)
                    "Failed" -> _uiState.value = PaymentUiState.Error("Payment failed or cancelled.")
                    else -> { /* Keep Processing */ }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = PaymentUiState.Idle
    }
}
