package com.example.luminarysolutions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.data.repository.AuthStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val authStatus: StateFlow<AuthStatus> = repository.getAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthStatus.Loading
        )

    fun signOut() {
        repository.signOut()
    }
}
