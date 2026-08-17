package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientProfileUiState())
    val uiState: StateFlow<ClientProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                userRepository.getUserProfile(currentUser.uid).collect { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, isLoggedOut = true) }
        }
    }

    /**
     * Updates the user's personal information.
     */
    fun updateProfile(name: String, phone: String, bio: String) {
        val currentUser = _uiState.value.user ?: return
        val updatedUser = currentUser.copy(
            name = name,
            phoneNumber = phone,
            bio = bio
        )
        performUpdate(updatedUser)
    }

    /**
     * Toggles notification preference.
     */
    fun toggleNotifications(enabled: Boolean) {
        val currentUser = _uiState.value.user ?: return
        performUpdate(currentUser.copy(notificationsEnabled = enabled))
    }

    /**
     * Toggles dark mode preference.
     */
    fun toggleDarkMode(enabled: Boolean) {
        val currentUser = _uiState.value.user ?: return
        performUpdate(currentUser.copy(darkModeEnabled = enabled))
    }

    /**
     * Toggles two-factor authentication preference.
     */
    fun toggleTwoFactor(enabled: Boolean) {
        val currentUser = _uiState.value.user ?: return
        performUpdate(currentUser.copy(isTwoFactorEnabled = enabled))
    }

    private fun performUpdate(user: User) {
        _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(user)
            _uiState.update { state ->
                state.copy(
                    isUpdating = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(isLoggedOut = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
