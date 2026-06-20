package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for processing donations.
 */
class DonationViewModel : ViewModel() {
    private val repository = ProjectsRepository()

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Idle)
    val uiState: StateFlow<DonationUiState> = _uiState

    /**
     * Processes a donation.
     * @param amount The donation amount.
     * @param method The payment method (e.g., "M-Pesa", "Card").
     * @param donorId Optional ID of the registered user.
     */
    fun processDonation(amount: Int, method: String, donorId: String?) {
        if (amount <= 0) {
            _uiState.value = DonationUiState.Error("Invalid amount.")
            return
        }

        _uiState.value = DonationUiState.Loading

        viewModelScope.launch {
            repository.addDonation(donorId, amount, method) { success ->
                if (success) {
                    _uiState.value = DonationUiState.Success
                } else {
                    _uiState.value = DonationUiState.Error("Transaction failed. Please try again.")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = DonationUiState.Idle
    }
}

sealed class DonationUiState {
    object Idle : DonationUiState()
    object Loading : DonationUiState()
    object Success : DonationUiState()
    data class Error(val message: String) : DonationUiState()
}
