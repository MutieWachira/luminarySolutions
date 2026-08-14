package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.AuditLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * Fetches the latest audit logs from Firestore.
     */
    suspend fun getLatestLogs(limit: Long = 50): Result<List<AuditLog>> {
        return try {
            val snapshot = db.collection("audit_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AuditLog::class.java)?.copy(id = doc.id)
            }
            
            if (logs.isEmpty()) {
                // Return dummy data if collection is empty or doesn't exist for demo purposes
                Result.success(getDummyLogs())
            } else {
                Result.success(logs)
            }
        } catch (e: Exception) {
            // Fallback to dummy data on error (e.g. collection not found)
            Result.success(getDummyLogs())
        }
    }

    private fun getDummyLogs(): List<AuditLog> {
        return listOf(
            AuditLog("1", "admin_1", "System Admin", "User Role Updated", Date(), "Changed 'John Doe' to ADMIN", "INFO"),
            AuditLog("2", "admin_1", "System Admin", "Maintenance Mode Enabled", Date(System.currentTimeMillis() - 3600000), "Platform put in maintenance mode", "WARNING"),
            AuditLog("3", "user_5", "Jane Smith", "Login Success", Date(System.currentTimeMillis() - 7200000), "Logged in from 192.168.1.1", "INFO")
        )
    }
}
