package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.auth.safeValueOf
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage
) {
    private fun getTeamsCollection() = db.collection("luminary").document("teams").collection("items")

    fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val reg = db.collection("users").document(userId).addSnapshotListener { doc, _ ->
            trySend(doc?.let { if (it.exists()) mapToUser(it) else null })
        }
        awaitClose { reg.remove() }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(mapFromUser(user), SetOptions.merge()).await()
            // Sync with teams collection if needed
            val teamUpdates = hashMapOf<String, Any?>(
                "name" to user.name,
                "phone" to user.phoneNumber,
                "bio" to user.bio,
                "imageUrl" to user.profileImageUrl,
                "isTwoFactorEnabled" to user.isTwoFactorEnabled
            )
            val teamDoc = getTeamsCollection().document(user.id).get().await()
            if (teamDoc.exists()) {
                getTeamsCollection().document(user.id).update(teamUpdates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTeamMembers(): Flow<List<User>> = callbackFlow {
        val reg = db.collection("users").addSnapshotListener { snp, _ ->
            trySend(snp?.documents?.mapNotNull { mapToUser(it) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = db.collection("users").whereIn(FieldPath.documentId(), ids)
            .addSnapshotListener { snp, _ ->
                trySend(snp?.documents?.mapNotNull { mapToUser(it) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun getTeamsByIds(ids: List<String>): Flow<List<Team>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = getTeamsCollection().whereIn(FieldPath.documentId(), ids)
            .addSnapshotListener { snp, _ ->
                trySend(snp?.documents?.mapNotNull { mapToTeam(it) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun updateTeamMember(team: Team): Result<Unit> {
        return try {
            if (team.id.isEmpty()) return Result.failure(Exception("Team member ID is empty"))
            val updates = mapFromTeam(team).apply {
                remove("createdAt")
            }
            getTeamsCollection().document(team.id).update(updates as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTeamMember(team: Team): Result<Unit> {
        return try {
            getTeamsCollection().add(mapFromTeam(team)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTeamMember(teamId: String): Result<Unit> {
        return try {
            getTeamsCollection().document(teamId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTeamProfileByEmail(email: String): Flow<Team?> = callbackFlow {
        val registration = getTeamsCollection()
            .whereEqualTo("email", email)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.firstOrNull()?.let { mapToTeam(it) })
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateTeamProfile(team: Team): Result<Unit> {
        return try {
            if (team.id.isEmpty()) return Result.failure(Exception("Team member ID is empty"))
            val updates = hashMapOf<String, Any?>(
                "name" to team.name,
                "phone" to team.phone,
                "phoneNumber" to team.phone,
                "bio" to team.bio,
                "gender" to team.gender,
                "imageUrl" to team.imageUrl,
                "profileImageUrl" to team.imageUrl,
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

    suspend fun checkUserExistsByEmail(email: String): Result<Boolean> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVolunteerUser(volunteer: com.example.luminarysolutions.data.models.Volunteer): Result<Unit> {
        return try {
            val user = User(
                id = volunteer.id,
                name = volunteer.name,
                email = volunteer.email,
                role = UserRole.VOLUNTEER,
                enabled = true
            )
            db.collection("users").document(user.id).set(mapFromUser(user)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a profile image to Firebase Storage and returns the download URL.
     */
    suspend fun uploadProfileImage(userId: String, imageUri: android.net.Uri): Result<String> {
        return try {
            val ref = storage.reference.child("profile_images/$userId.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUser(doc: com.google.firebase.firestore.DocumentSnapshot) = User(
        id = doc.id,
        name = doc.getString("name") ?: "Unnamed",
        email = doc.getString("email") ?: "",
        phoneNumber = doc.getString("phoneNumber") ?: "",
        bio = doc.getString("bio") ?: "",
        profileImageUrl = doc.getString("profileImageUrl"),
        role = safeValueOf(doc.getString("role")),
        enabled = doc.getBoolean("enabled") ?: true,
        fcmToken = doc.getString("fcmToken"),
        isTwoFactorEnabled = doc.getBoolean("isTwoFactorEnabled") ?: false,
        darkModeEnabled = doc.getBoolean("darkModeEnabled") ?: false,
        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
    )

    private fun mapFromUser(u: User) = hashMapOf(
        "name" to u.name,
        "email" to u.email,
        "phoneNumber" to u.phoneNumber,
        "bio" to u.bio,
        "profileImageUrl" to u.profileImageUrl,
        "role" to u.role.name,
        "enabled" to u.enabled,
        "fcmToken" to u.fcmToken,
        "isTwoFactorEnabled" to u.isTwoFactorEnabled,
        "darkModeEnabled" to u.darkModeEnabled,
        "notificationsEnabled" to u.notificationsEnabled
    )

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
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}
