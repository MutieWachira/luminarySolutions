package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Enquiry
import com.example.luminarysolutions.data.models.Freelance
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
 * UI State for the Service Enquiry screen.
 */
data class EnquiryUiState(
    val service: Freelance? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

/**
 * ViewModel for drafting and sending a service inquiry.
 * Follows MVVM architecture and leverages Hilt for dependency injection.
 */
@HiltViewModel
class ClientServiceEnquiryViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnquiryUiState())
    val uiState: StateFlow<EnquiryUiState> = _uiState.asStateFlow()

    /**
     * Loads the service details to provide context for the enquiry.
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
     * Sends the drafted enquiry to the server.
     * Accounts for user authentication and profile data.
     */
    fun sendEnquiry(subject: String, message: String) {
        val service = _uiState.value.service ?: return
        if (subject.isBlank() || message.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Subject and message cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, error = null)
            
            // Get current user authentication status
            val authStatus = authRepository.getAuthState().firstOrNull { it !is AuthStatus.Loading }
            
            if (authStatus is AuthStatus.Authenticated) {
                // Fetch the detailed user profile
                val user = userRepository.getUserProfile(authStatus.uid).firstOrNull()
                if (user != null) {
                    val enquiry = Enquiry(
                        serviceId = service.id,
                        serviceName = service.name,
                        clientId = user.id,
                        clientName = user.name,
                        subject = subject,
                        message = message
                    )
                    
                    // Send the enquiry through the repository
                    clientRepository.sendEnquiry(enquiry)
                        .onSuccess {
                            _uiState.value = _uiState.value.copy(isSending = false, success = true)
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(isSending = false, error = e.message ?: "Failed to send enquiry")
                        }
                } else {
                    _uiState.value = _uiState.value.copy(isSending = false, error = "User profile not found. Please complete your profile.")
                }
            } else {
                _uiState.value = _uiState.value.copy(isSending = false, error = "You must be logged in to send an enquiry.")
            }
        }
    }

    /**
     * Resets the success state to allow for navigation or UI updates.
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }
}
