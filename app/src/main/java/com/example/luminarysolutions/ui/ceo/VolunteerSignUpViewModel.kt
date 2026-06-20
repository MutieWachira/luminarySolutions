package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Volunteer Sign-Up process.
 * Manages the state of the sign-up form and interaction with the repository.
 */
class VolunteerSignUpViewModel : ViewModel() {
    private val repository = ProjectsRepository()

    private val _uiState = MutableStateFlow<VolunteerSignUpUiState>(VolunteerSignUpUiState.Idle)
    val uiState: StateFlow<VolunteerSignUpUiState> = _uiState

    /**
     * Submits a new volunteer application.
     * @param name The full name of the volunteer.
     * @param email The email address.
     * @param phone The contact phone number.
     * @param skills A list of skills provided by the volunteer.
     * @param motivation The reason for joining.
     * @param projectId Optional project ID the user is applying for.
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

        val volunteer = Volunteer(
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
            try {
                repository.addVolunteerApplication(volunteer) { success ->
                    if (success) {
                        _uiState.value = VolunteerSignUpUiState.Success
                    } else {
                        _uiState.value = VolunteerSignUpUiState.Error("Failed to submit application. Please check your network and try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = VolunteerSignUpUiState.Error("Submission failed: ${e.message}")
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
