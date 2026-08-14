package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.TeamCulture
import com.example.luminarysolutions.data.services.EmailService
import com.example.luminarysolutions.ui.auth.safeValueOf
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun getTeamsCollection() = db.collection("luminary").document("teams").collection("items")

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
     */
    suspend fun addTeamMember(team: Team): Result<Unit> {
        return try {
            if (team.email.isBlank()) {
                return Result.failure(Exception("Email is required."))
            }
            getTeamsCollection().add(mapFromTeam(team)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing team member's record.
     */
    suspend fun updateTeamMember(team: Team): Result<Unit> {
        return try {
            if (team.id.isEmpty()) return Result.failure(Exception("Team member ID is empty"))
            val updates = mapFromTeam(team).apply {
                remove("createdAt") // Prevent resetting creation date on updates
            }
            getTeamsCollection().document(team.id).update(updates as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a team member record.
     */
    suspend fun deleteTeamMember(uid: String): Result<Unit> {
        return try {
            getTeamsCollection().document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Surgically updates a team member's profile.
     * Synchronizes changes with the global 'users' collection.
     */
    suspend fun updateTeamProfile(team: Team): Result<Unit> {
        return try {
            if (team.id.isEmpty()) return Result.failure(Exception("Team member ID is empty"))
            val updates = hashMapOf<String, Any?>(
                "name" to team.name,
                "phone" to team.phone,
                "phoneNumber" to team.phone, // Sync with User model
                "bio" to team.bio,
                "gender" to team.gender,
                "imageUrl" to team.imageUrl,
                "profileImageUrl" to team.imageUrl, // Sync with User model
                "isTwoFactorEnabled" to team.isTwoFactorEnabled
            )
            
            val batch = db.batch()
            batch.update(getTeamsCollection().document(team.id), updates as Map<String, Any>)
            batch.update(db.collection("users").document(team.id), updates)
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all team members as a one-shot operation.
     */
    suspend fun getTeams(): Result<List<Team>> {
        return try {
            val result = getTeamsCollection().get().await()
            val teams = result.documents.mapNotNull { mapToTeam(it) }
            Result.success(teams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy method for fetching all team members.
     */
    suspend fun getTeamMembers(): List<Team> = getTeams().getOrDefault(emptyList())

    /**
     * Fetches specific team members by their unique IDs as a one-shot operation.
     */
    suspend fun getTeamsByIds(ids: List<String>): Result<List<Team>> {
        return try {
            if (ids.isEmpty()) return Result.success(emptyList())
            
            val result = getTeamsCollection()
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
                .get()
                .await()
            
            val teams = result.documents.mapNotNull { mapToTeam(it) }
            Result.success(teams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of all team members.
     */
    fun getTeamsFlow(): Flow<List<Team>> = callbackFlow {
        val registration = getTeamsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { mapToTeam(it) } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Real-time stream of specific team members by IDs.
     */
    fun getTeamsByIdsFlow(ids: List<String>): Flow<List<Team>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = getTeamsCollection()
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { mapToTeam(it) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches a team profile by email.
     */
    fun getTeamProfileByEmail(email: String): Flow<Team?> = callbackFlow {
        val registration = getTeamsCollection()
            .whereEqualTo("email", email)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.firstOrNull()?.let { mapToTeam(it) })
            }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches the team culture stats.
     */
    fun getTeamCulture(): Flow<TeamCulture> = callbackFlow {
        val registration = db.collection("luminary").document("culture").addSnapshotListener { doc, _ ->
            trySend(doc?.takeIf { it.exists() }?.let {
                TeamCulture(
                    diversityRate = it.getString("diversityRate") ?: "38%",
                    satisfactionScore = it.getString("satisfactionScore") ?: "4.6/5",
                    trainingPrograms = it.getLong("trainingPrograms")?.toInt() ?: 24
                )
            } ?: TeamCulture())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Tests SMTP configuration.
     */
    suspend fun testSmtp(): Result<String> {
        return EmailService.testSmtpConnection()
    }

    // MAPPING UTILITIES
    private fun mapToTeam(doc: com.google.firebase.firestore.DocumentSnapshot) = Team(
        id = doc.id, 
        imageUrl = doc.getString("imageUrl"), 
        name = doc.getString("name") ?: "Unnamed",
        email = doc.getString("email") ?: "", 
        phone = doc.getString("phone") ?: "", 
        department = doc.getString("department") ?: "",
        jobtitle = doc.getString("jobtitle") ?: "", 
        gender = doc.getString("gender") ?: "Male",
        bio = doc.getString("bio") ?: "",
        role = safeValueOf(doc.getString("role")), 
        enabled = doc.getBoolean("enabled") ?: true,
        isTwoFactorEnabled = doc.getBoolean("isTwoFactorEnabled") ?: false,
        datejoined = (doc.get("createdAt") as? Timestamp)?.toDate()?.time ?: (doc.get("datejoined") as? Number)?.toLong() ?: System.currentTimeMillis()
    )

    private fun mapFromTeam(t: Team) = hashMapOf(
        "name" to t.name, 
        "email" to t.email, 
        "phone" to t.phone, 
        "department" to t.department, 
        "jobtitle" to t.jobtitle,
        "gender" to t.gender, 
        "bio" to t.bio, 
        "role" to t.role.name, 
        "enabled" to t.enabled, 
        "isTwoFactorEnabled" to t.isTwoFactorEnabled,
        "imageUrl" to t.imageUrl, 
        "createdAt" to FieldValue.serverTimestamp()
    )
}
