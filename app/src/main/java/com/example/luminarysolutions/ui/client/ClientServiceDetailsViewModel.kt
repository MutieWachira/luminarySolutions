package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Service Details screen.
 */
data class ServiceDetailsUiState(
    val service: Freelance? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isApplying: Boolean = false,
    val applicationSuccess: Boolean = false
)

/**
 * ViewModel for viewing details of a specific service and initiating inquiries.
 */
@HiltViewModel
class ClientServiceDetailsViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailsUiState())
    val uiState: StateFlow<ServiceDetailsUiState> = _uiState.asStateFlow()

    fun loadService(serviceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getServiceById(serviceId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { service ->
                    _uiState.value = _uiState.value.copy(isLoading = false, service = service)
                }
        }
    }

    fun applyForService(serviceId: String, clientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            repository.applyForService(serviceId, clientId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isApplying = false, applicationSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isApplying = false, error = e.message)
                }
        }
    }

    fun clearApplicationSuccess() {
        _uiState.value = _uiState.value.copy(applicationSuccess = false)
    }
}
