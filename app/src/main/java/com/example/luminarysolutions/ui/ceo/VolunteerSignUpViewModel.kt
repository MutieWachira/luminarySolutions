package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Volunteer Sign-Up process.
 */
@HiltViewModel
class VolunteerSignUpViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VolunteerSignUpUiState>(VolunteerSignUpUiState.Idle)
    val uiState: StateFlow<VolunteerSignUpUiState> = _uiState

    /**
     * Submits a new volunteer application.
     */
    fun submitApplication(
        name: String,
        email: String,
        phone: String,
        skills: String,
        motivation: String = "",
        projectId: String?
    ) {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            _uiState.value = VolunteerSignUpUiState.Error("Please fill in all required fields.")
            return
        }

        _uiState.value = VolunteerSignUpUiState.Loading

        val volunteer = com.example.luminarysolutions.data.models.Volunteer(
            id = "", // Firestore will generate an ID
            name = name,
            email = email,
            phoneNumber = phone,
            skills = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            motivation = motivation,
            status = "Pending",
            appliedDate = System.currentTimeMillis(),
            projectIds = if (projectId != null) listOf(projectId) else emptyList()
        )

        viewModelScope.launch {
            repository.addVolunteerApplication(volunteer).onSuccess {
                _uiState.value = VolunteerSignUpUiState.Success
            }.onFailure { error ->
                _uiState.value = VolunteerSignUpUiState.Error(error.message ?: "Failed to submit application. Please check your network and try again.")
            }
        }
    }

    fun resetState() {
        _uiState.value = VolunteerSignUpUiState.Idle
    }
}

sealed class VolunteerSignUpUiState {
    object Idle : VolunteerSignUpUiState()
    object Loading : VolunteerSignUpUiState()
    object Success : VolunteerSignUpUiState()
    data class Error(val message: String) : VolunteerSignUpUiState()
}
