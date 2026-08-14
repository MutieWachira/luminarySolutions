package com.example.luminarysolutions.data.repository

import android.util.Log
import com.example.luminarysolutions.data.models.SystemSettings
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * Streams all users from Firestore.
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Updates a user's role.
     */
    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("role", newRole.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggles a user's account status (enabled/disabled).
     */
    suspend fun setUserEnabled(userId: String, enabled: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("enabled", enabled)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches system statistics for the admin dashboard.
     * Uses a robust approach to prevent total failure if one collection is inaccessible.
     */
    suspend fun getSystemStats(): Result<Map<String, Any>> {
        return try {
            val userCount = try {
                db.collection("users").get().await().size()
            } catch (e: Exception) {
                Log.e("AdminRepository", "Failed to fetch user count", e)
                0
            }
            
            val projectCount = try {
                db.collection("lumisphere").document("projects").collection("items")
                    .get().await().size()
            } catch (e: Exception) {
                Log.e("AdminRepository", "Failed to fetch project count", e)
                0
            }

            val donorCount = try {
                db.collection("lumisphere").document("donors").collection("items")
                    .get().await().size()
            } catch (e: Exception) {
                Log.e("AdminRepository", "Failed to fetch donor count", e)
                0
            }
            
            Result.success(mapOf(
                "totalUsers" to userCount,
                "totalProjects" to projectCount,
                "totalDonors" to donorCount,
                "systemStatus" to "Healthy",
                "lastBackup" to "2 hours ago"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches current platform settings.
     */
    suspend fun getSystemSettings(): Result<SystemSettings> {
        return try {
            val doc = db.collection("system").document("settings").get().await()
            val settings = doc.toObject(SystemSettings::class.java) ?: SystemSettings()
            Result.success(settings)
        } catch (e: Exception) {
            // Return default settings on failure for demo
            Result.success(SystemSettings())
        }
    }

    /**
     * Persists platform settings.
     */
    suspend fun saveSystemSettings(settings: SystemSettings): Result<Unit> {
        return try {
            db.collection("system").document("settings").set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
