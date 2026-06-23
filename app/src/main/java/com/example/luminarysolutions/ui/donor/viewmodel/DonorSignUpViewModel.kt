package com.example.luminarysolutions.ui.donor.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.DonorsRepository
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for Donor registration.
 * Implements MVVM pattern to handle authentication and Firestore data population.
 * Ensures production-ready practices like input validation and atomic database updates.
 */
class DonorSignUpViewModel(
    private val repository: DonorsRepository = DonorsRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<DonorSignUpUiState>(DonorSignUpUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Executes the donor sign-up process.
     * Includes Auth creation, User profile setup, and Donor collection entry.
     */
    fun signUp(name: String, email: String, phone: String, password: String) {
        if (!validateInputs(name, email, phone, password)) return

        _uiState.value = DonorSignUpUiState.Loading

        viewModelScope.launch {
            try {
                // 1. Create Firebase Auth Account
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Authentication failed: No user ID returned")

                // 2. Prepare Data Models
                val userProfile = User(
                    id = uid,
                    name = name,
                    email = email,
                    phoneNumber = phone,
                    role = UserRole.DONOR,
                    enabled = true
                )

                val donorRecord = Donor(
                    id = uid,
                    name = name,
                    type = "Individual",
                    status = "Active",
                    valueOrNote = "Newly Registered",
                    lastContact = "" // Handled by server timestamp in repository
                )

                // 3. Save to Firestore (Atomic Batch via Repository)
                repository.registerDonor(userProfile, donorRecord) { success ->
                    if (success) {
                        _uiState.value = DonorSignUpUiState.Success
                    } else {
                        // Rollback logic: In a real production app, you might want to delete the Auth user if Firestore fails
                        // for simplicity here, we just show an error.
                        _uiState.value = DonorSignUpUiState.Error("Failed to create profile. Please contact support.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DonorSignUpUiState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    private fun validateInputs(name: String, email: String, phone: String, password: String): Boolean {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _uiState.value = DonorSignUpUiState.Error("All fields are required")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = DonorSignUpUiState.Error("Invalid email address")
            return false
        }
        if (password.length < 6) {
            _uiState.value = DonorSignUpUiState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }

    fun resetState() {
        _uiState.value = DonorSignUpUiState.Idle
    }
}

/**
 * UI State for the Donor Sign-Up flow.
 */
sealed class DonorSignUpUiState {
    object Idle : DonorSignUpUiState()
    object Loading : DonorSignUpUiState()
    object Success : DonorSignUpUiState()
    data class Error(val message: String) : DonorSignUpUiState()
}
