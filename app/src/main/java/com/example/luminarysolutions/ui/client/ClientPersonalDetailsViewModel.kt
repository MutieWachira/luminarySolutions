package com.example.luminarysolutions.ui.client

import android.net.Uri
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

data class PersonalDetailsUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ClientPersonalDetailsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalDetailsUiState())
    val uiState: StateFlow<PersonalDetailsUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in") }
        }
    }

    /**
     * Updates general profile information and image if provided.
     */
    fun updateProfile(name: String, phone: String, bio: String, imageUri: Uri? = null) {
        val currentUser = _uiState.value.user ?: return
        _uiState.update { it.copy(isUpdating = true, errorMessage = null, updateSuccess = false) }

        viewModelScope.launch {
            var finalImageUrl = currentUser.profileImageUrl
            
            // Upload image if selected
            if (imageUri != null) {
                val uploadResult = userRepository.uploadProfileImage(currentUser.id, imageUri)
                if (uploadResult.isSuccess) {
                    finalImageUrl = uploadResult.getOrNull()
                } else {
                    _uiState.update { it.copy(isUpdating = false, errorMessage = "Failed to upload image") }
                    return@launch
                }
            }

            val updatedUser = currentUser.copy(
                name = name,
                phoneNumber = phone,
                bio = bio,
                profileImageUrl = finalImageUrl
            )

            val result = userRepository.updateUserProfile(updatedUser)
            if (result.isSuccess) {
                _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
            } else {
                _uiState.update { it.copy(isUpdating = false, errorMessage = result.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    /**
     * Updates user email. Requires re-authentication.
     */
    fun updateEmail(newEmail: String, currentPassword: String) {
        _uiState.update { it.copy(isUpdating = true, errorMessage = null, updateSuccess = false) }
        viewModelScope.launch {
            // 1. Re-authenticate
            val reAuthResult = authRepository.reauthenticate(currentPassword)
            if (reAuthResult.isFailure) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = "Incorrect password") }
                return@launch
            }

            // 2. Update Email in Auth
            val authResult = authRepository.updateEmail(newEmail)
            if (authResult.isFailure) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = authResult.exceptionOrNull()?.localizedMessage) }
                return@launch
            }

            // 3. Update Email in Firestore
            val currentUser = _uiState.value.user ?: return@launch
            val result = userRepository.updateUserProfile(currentUser.copy(email = newEmail))
            
            if (result.isSuccess) {
                _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
            } else {
                _uiState.update { it.copy(isUpdating = false, errorMessage = result.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    /**
     * Updates user password. Requires re-authentication.
     */
    fun updatePassword(currentPassword: String, newPassword: String) {
        _uiState.update { it.copy(isUpdating = true, errorMessage = null, updateSuccess = false) }
        viewModelScope.launch {
            // 1. Re-authenticate
            val reAuthResult = authRepository.reauthenticate(currentPassword)
            if (reAuthResult.isFailure) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = "Incorrect current password") }
                return@launch
            }

            // 2. Update Password
            val result = authRepository.updatePassword(newPassword)
            if (result.isSuccess) {
                _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
            } else {
                _uiState.update { it.copy(isUpdating = false, errorMessage = result.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(updateSuccess = false) }
    }
}
