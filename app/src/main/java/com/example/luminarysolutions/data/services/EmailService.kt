package com.example.luminarysolutions.data.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Service to handle automated email notifications using Firebase Cloud Functions.
 */
object EmailService {

    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance("us-central1").apply {
        // useEmulator("10.0.2.2", 5001) // Uncomment if using the Firebase Emulator
    }

    /**
     * Sends a volunteer status email via a Firebase Cloud Function.
     */
    suspend fun sendVolunteerStatusEmail(
        email: String,
        name: String,
        status: String,
        password: String? = null
    ) {
        val data = hashMapOf(
            "email" to email,
            "name" to name,
            "status" to status,
            "password" to password
        )

        try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            functions
                .getHttpsCallable("sendVolunteerStatusEmail")
                .call(data)
                .await()
            Log.i("EmailService", "Email request successfully sent to backend for $email")
        } catch (e: Exception) {
            Log.e("EmailService", "Failed to trigger email for $email via Cloud Functions", e)
        }
    }

    /**
     * Tests the SMTP connection on the backend.
     */
    suspend fun testSmtpConnection(): Result<String> {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            val result = functions
                .getHttpsCallable("testSmtpConnection")
                .call()
                .await()
            val data = result.data as? Map<*, *>
            val message = data?.get("message")?.toString() ?: "No message returned"
            Result.success(message)
        } catch (e: Exception) {
            Log.e("EmailService", "SMTP Test Failed", e)
            Result.failure(e)
        }
    }
}
