package com.example.luminarysolutions.ui.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.ui.login.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _registerSuccess = mutableStateOf(false)
    val registerSuccess: State<Boolean> = _registerSuccess

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
    }

    fun onRegisterClick() {
        _errorMessage.value = ""

        if (_email.value.isBlank() ||
            _password.value.isBlank() ||
            _confirmPassword.value.isBlank()
        ) {
            _errorMessage.value = "All fields are required"
            return
        }

        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.register(
                email = _email.value,
                password = _password.value
            )

            _isLoading.value = false

            result.onSuccess {
                _registerSuccess.value = true
            }

            result.onFailure {
                _errorMessage.value = it.message ?: "Registration failed"
            }
        }
    }
}
