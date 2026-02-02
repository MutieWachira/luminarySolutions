package com.example.luminarysolutions.ui.login

sealed class LoginUiState{
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Success : LoginUiState()
}