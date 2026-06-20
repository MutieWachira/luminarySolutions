package com.example.luminarysolutions.ui.donor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DonationViewModel(
    private val repository: ProjectsRepository = ProjectsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _amount = MutableStateFlow(0)
    val amount = _amount.asStateFlow()

    fun setAmount(value: Int) {
        _amount.value = value
    }

    fun processDonation(donorId: String?, method: String, details: Map<String, String> = emptyMap()) {
        if (_amount.value <= 0) {
            _uiState.value = DonationUiState.Error("Please enter a valid amount")
            return
        }

        _uiState.value = DonationUiState.Processing
        viewModelScope.launch {
            repository.addDonation(donorId, _amount.value, method, details = details) { success ->
                if (success) {
                    _uiState.value = DonationUiState.Success
                } else {
                    _uiState.value = DonationUiState.Error("Payment failed. Please try again.")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = DonationUiState.Idle
        _amount.value = 0
    }
}

sealed class DonationUiState {
    object Idle : DonationUiState()
    object Processing : DonationUiState()
    object Success : DonationUiState()
    data class Error(val message: String) : DonationUiState()
}