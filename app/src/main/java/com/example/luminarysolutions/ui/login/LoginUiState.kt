package com.example.luminarysolutions.ui.login

sealed class LoginUiState{
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class Info(val message: String) : LoginUiState()
    data class Success(val role: com.example.luminarysolutions.ui.auth.UserRole) : LoginUiState()
}