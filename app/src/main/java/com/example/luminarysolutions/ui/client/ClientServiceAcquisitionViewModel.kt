package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.ServiceAcquisition
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.data.repository.AuthStatus
import com.example.luminarysolutions.data.repository.ClientRepository
import com.example.luminarysolutions.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Service Acquisition screen.
 */
data class AcquisitionUiState(
    val service: Freelance? = null,
    val isLoading: Boolean = false,
    val isAcquiring: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

/**
 * ViewModel for processing service acquisition requests.
 * Manages the transition from intent to official service request.
 */
@HiltViewModel
class ClientServiceAcquisitionViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcquisitionUiState())
    val uiState: StateFlow<AcquisitionUiState> = _uiState.asStateFlow()

    /**
     * Loads the service details to confirm the acquisition intent.
     */
    fun loadService(serviceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            clientRepository.getServiceById(serviceId).collect { service ->
                _uiState.value = _uiState.value.copy(isLoading = false, service = service)
            }
        }
    }

    /**
     * Finalizes the service acquisition request.
     * Records the request in the system for administrative approval and project kickoff.
     */
    fun acquireService() {
        val service = _uiState.value.service ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAcquiring = true, error = null)
            
            val authStatus = authRepository.getAuthState().firstOrNull { it !is AuthStatus.Loading }
            
            if (authStatus is AuthStatus.Authenticated) {
                val user = userRepository.getUserProfile(authStatus.uid).firstOrNull()
                if (user != null) {
                    val acquisition = ServiceAcquisition(
                        serviceId = service.id,
                        serviceName = service.name,
                        clientId = user.id,
                        clientName = user.name,
                        price = service.price
                    )
                    
                    clientRepository.acquireService(acquisition)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(isAcquiring = false, success = true)
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(isAcquiring = false, error = e.message ?: "Failed to process acquisition")
                        }
                } else {
                    _uiState.value = _uiState.value.copy(isAcquiring = false, error = "User profile not found")
                }
            } else {
                _uiState.value = _uiState.value.copy(isAcquiring = false, error = "User not authenticated")
            }
        }
    }
}
