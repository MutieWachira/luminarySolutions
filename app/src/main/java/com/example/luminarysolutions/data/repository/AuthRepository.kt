package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.auth.safeValueOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Observes the current authentication state and fetches the user role.
     */
    fun getAuthState(): Flow<AuthStatus> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(AuthStatus.Unauthenticated)
            } else {
                // Fetch role from Firestore
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val role = safeValueOf(doc.getString("role"))
                        val isEnabled = doc.getBoolean("enabled") ?: true
                        if (isEnabled) {
                            trySend(AuthStatus.Authenticated(currentUser.uid, role))
                        } else {
                            auth.signOut()
                            trySend(AuthStatus.Error("Account disabled"))
                        }
                    }
                    .addOnFailureListener { e ->
                        trySend(AuthStatus.Error(e.localizedMessage ?: "Sync failed"))
                    }
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose { auth.removeAuthStateListener(authListener) }
    }

    suspend fun signIn(email: String, password: String): Result<UserRole> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User not found")
            
            val doc = db.collection("users").document(uid).get().await()
            val isEnabled = doc.getBoolean("enabled") ?: true
            if (!isEnabled) {
                auth.signOut()
                throw Exception("Account disabled")
            }
            
            val role = safeValueOf(doc.getString("role"))
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class AuthStatus {
    object Unauthenticated : AuthStatus()
    data class Authenticated(val uid: String, val role: UserRole) : AuthStatus()
    data class Error(val message: String) : AuthStatus()
    object Loading : AuthStatus()
}
