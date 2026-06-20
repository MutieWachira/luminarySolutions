package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.services.EmailService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TeamRepository @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Checks if a user with the given email already exists in the 'users' collection.
     */
    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val result = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Adds a new team member.
     * The app writes to the 'teams' collection, which triggers a Cloud Function 
     * to handle Auth creation, user document population, and emails.
     */
    suspend fun addTeamMember(team: Team): Result<Unit> = suspendCancellableCoroutine { continuation ->
        if (team.email.isBlank()) {
            continuation.resume(Result.failure(Exception("Email is required.")))
            return@suspendCancellableCoroutine
        }

        // Add to teams collection - Backend trigger handles the rest
        FirestoreService.addTeamMember(team) { success ->
            if (success) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(Exception("Failed to add team member to Firestore.")))
            }
        }
    }

    /**
     * Deletes a team member record.
     * Backend triggers will handle any additional cleanup or notification if configured.
     */
    suspend fun deleteTeamMember(uid: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        FirestoreService.deleteTeamMember(uid) { success ->
            if (success) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(Exception("Failed to delete team member record.")))
            }
        }
    }

    /**
     * Tests SMTP configuration.
     */
    suspend fun testSmtp(): Result<String> {
        return EmailService.testSmtpConnection()
    }

    /**
     * Fetches all team members.
     */
    suspend fun getTeamMembers(): List<Team> {
        return try {
            val result = db.collection("luminary")
                .document("teams")
                .collection("items")
                .get()
                .await()
            
            result.documents.mapNotNull { doc ->
                doc.toObject(Team::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
