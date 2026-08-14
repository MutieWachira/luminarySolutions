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
    val isLoggedOut: Boolean = false
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

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
