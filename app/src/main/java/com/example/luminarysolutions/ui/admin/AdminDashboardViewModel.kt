package com.example.luminarysolutions.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.Loading
            adminRepository.getSystemStats()
                .onSuccess { stats ->
                    _uiState.value = AdminDashboardUiState.Success(stats)
                }
                .onFailure { error ->
                    _uiState.value = AdminDashboardUiState.Error(error.message ?: "Failed to load stats")
                }
        }
    }
}

sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    data class Success(val stats: Map<String, Any>) : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
}
