package com.example.luminarysolutions.ui.volunteer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.UserRepository
import com.example.luminarysolutions.data.repository.VolunteerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteerSignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val volunteerRepository: VolunteerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VolunteerSignUpUiState>(VolunteerSignUpUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Submits a volunteer application.
     * Checks for duplicate accounts before submission.
     */
    fun submitApplication(
        name: String, 
        email: String, 
        phone: String, 
        skills: String, 
        motivation: String = "", 
        projectId: String? = null
    ) {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            _uiState.value = VolunteerSignUpUiState.Error("Please fill in all required fields")
            return
        }

        _uiState.value = VolunteerSignUpUiState.Loading

        viewModelScope.launch {
            try {
                // Step 1: Check if account already exists
                userRepository.checkUserExistsByEmail(email).onSuccess { exists ->
                    if (exists) {
                        _uiState.value = VolunteerSignUpUiState.Error("An account with this email already exists. Please login instead.")
                    } else {
                        // Step 2: Proceed with application
                        val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val volunteer = Volunteer(
                            name = name,
                            email = email,
                            phoneNumber = phone,
                            skills = skillsList,
                            motivation = motivation,
                            status = "Pending",
                            projectIds = if (projectId != null) listOf(projectId) else emptyList()
                        )

                        volunteerRepository.addVolunteerApplication(volunteer).onSuccess {
                            _uiState.value = VolunteerSignUpUiState.Success
                        }.onFailure {
                            _uiState.value = VolunteerSignUpUiState.Error("Failed to submit application. Please check your connection.")
                        }
                    }
                }.onFailure {
                    _uiState.value = VolunteerSignUpUiState.Error("Failed to check user existence: ${it.message}")
                }
            } catch (e: Exception) {
                _uiState.value = VolunteerSignUpUiState.Error("An unexpected error occurred: ${e.message}")
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
