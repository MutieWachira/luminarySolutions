package com.example.luminarysolutions.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.auth.safeValueOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set


    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    var role by mutableStateOf<UserRole?>(null)
        private set

    fun onEmailChange(value: String) {
        email = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }


    fun login(email: String, password: String) {

        if (email.isBlank() || password.isBlank()) {
            uiState = LoginUiState.Error("Fields cannot be empty")
            return
        }

        uiState = LoginUiState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                if (uid == null) {
                    uiState = LoginUiState.Error("User not found")
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val roleString = doc.getString("role")
                        role = safeValueOf(roleString)
                        uiState = LoginUiState.Success
                    }
                    .addOnFailureListener {
                        uiState = LoginUiState.Error("Failed to fetch role")
                    }
            }
            .addOnFailureListener { exception ->
                uiState = LoginUiState.Error(
                    exception.localizedMessage ?: "Login failed"
                )
            }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            uiState = LoginUiState.Error("Enter your email to reset password")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                uiState = LoginUiState.Error(
                    "Password reset email sent"
                )
            }
            .addOnFailureListener { exception ->
                uiState = LoginUiState.Error(
                    exception.localizedMessage ?: "Failed to send reset email"
                )
            }
    }

}



