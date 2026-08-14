package com.example.luminarysolutions.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.ui.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _role = MutableStateFlow<UserRole?>(null)
    val role: StateFlow<UserRole?> = _role.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Fields cannot be empty")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            repository.signIn(email, password)
                .onSuccess { userRole ->
                    _role.value = userRole
                    _uiState.value = LoginUiState.Success(userRole)
                }
                .onFailure { exception ->
                    _uiState.value = LoginUiState.Error(
                        exception.localizedMessage ?: "Login failed"
                    )
                }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter your email to reset password")
            return
        }

        viewModelScope.launch {
            repository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = LoginUiState.Info("Password reset email sent")
                }
                .onFailure { exception ->
                    _uiState.value = LoginUiState.Error(
                        exception.localizedMessage ?: "Failed to send reset email"
                    )
                }
        }
    }

    fun resetLoginState() {
        _uiState.value = LoginUiState.Idle
        _role.value = null
        _email.value = ""
        _password.value = ""
    }
}
