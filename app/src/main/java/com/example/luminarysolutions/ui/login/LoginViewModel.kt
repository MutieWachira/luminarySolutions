package com.example.luminarysolutions.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginSuccess = mutableStateOf(false)
    val loginSuccess: State<Boolean> = _loginSuccess


    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onLoginClick() {
        _errorMessage.value = ""

        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }


        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.login(
                email = _email.value,
                password = _password.value
            )

            _isLoading.value = false

            result.onSuccess {
                _loginSuccess.value = true
            }

            result.onFailure {
                _errorMessage.value = it.message ?: "Login failed"
            }
        }
    }
    /**
     * Call this function after the navigation has occurred to reset the login state,
     * preventing re-navigation on configuration changes.
     */
    fun onNavigationComplete() {
        _loginSuccess.value = false
    }
}

