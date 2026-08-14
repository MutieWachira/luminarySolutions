package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun getNotificationsCollection() = db.collection("notifications")

    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val reg = getNotificationsCollection()
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snp, _ ->
                val list = snp?.documents?.mapNotNull { mapToNotification(it) } ?: emptyList()
                trySend(list.sortedByDescending { it.timestamp })
            }
        awaitClose { reg.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            getNotificationsCollection().document(notificationId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addNotification(notification: Notification): Result<Unit> {
        return try {
            getNotificationsCollection().add(mapFromNotification(notification)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToNotification(doc: com.google.firebase.firestore.DocumentSnapshot) = Notification(
        id = doc.id,
        userId = doc.getString("userId") ?: "",
        title = doc.getString("title") ?: "",
        message = doc.getString("message") ?: "",
        type = doc.getString("type") ?: "INFO",
        timestamp = (doc.get("timestamp") as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
        isRead = doc.getBoolean("isRead") ?: false
    )

    private fun mapFromNotification(n: Notification) = hashMapOf(
        "userId" to n.userId,
        "title" to n.title,
        "message" to n.message,
        "type" to n.type,
        "timestamp" to FieldValue.serverTimestamp(),
        "isRead" to n.isRead
    )
}
