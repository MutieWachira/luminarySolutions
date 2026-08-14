package com.example.luminarysolutions.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.UserRepository
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for UnifiedAchievementsScreen.
 */
@HiltViewModel
class UnifiedAchievementsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UnifiedAchievementsUiState>(UnifiedAchievementsUiState.Loading)
    val uiState: StateFlow<UnifiedAchievementsUiState> = _uiState.asStateFlow()

    init {
        detectUserRole()
    }

    private fun detectUserRole() {
        viewModelScope.launch {
            _uiState.value = UnifiedAchievementsUiState.Loading
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uiState.value = UnifiedAchievementsUiState.Error("No user logged in.")
                return@launch
            }

            userRepository.getUserProfile(uid).collect { user ->
                if (user != null) {
                    _uiState.value = UnifiedAchievementsUiState.Success(user.role)
                } else {
                    _uiState.value = UnifiedAchievementsUiState.Error("User record not found.")
                }
            }
        }
    }

    fun retry() {
        detectUserRole()
    }
}

sealed class UnifiedAchievementsUiState {
    object Loading : UnifiedAchievementsUiState()
    data class Success(val role: UserRole) : UnifiedAchievementsUiState()
    data class Error(val message: String) : UnifiedAchievementsUiState()
}
