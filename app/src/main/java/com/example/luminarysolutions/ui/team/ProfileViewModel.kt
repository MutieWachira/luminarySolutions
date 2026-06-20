package com.example.luminarysolutions.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Team
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun updateProfile(team: Team) {
        _uiState.update { it.copy(isUpdating = true, updateSuccess = null) }
        FirestoreService.updateTeamProfile(team) { success ->
            _uiState.update { it.copy(isUpdating = false, updateSuccess = success) }
        }
    }

    fun changePassword(newPassword: String) {
        _uiState.update { it.copy(isUpdating = true, updateSuccess = null) }
        auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            _uiState.update { it.copy(isUpdating = false, updateSuccess = task.isSuccessful, error = if (!task.isSuccessful) task.exception?.message else null) }
        }
    }

    fun toggle2FA(enabled: Boolean, team: Team) {
        // Multi-Factor Auth logic would go here. For now, we update the flag in Firestore.
        val updatedTeam = team.copy(isTwoFactorEnabled = enabled)
        updateProfile(updatedTeam)
    }
    
    fun resetUpdateStatus() {
        _uiState.update { it.copy(updateSuccess = null, error = null) }
    }
}
