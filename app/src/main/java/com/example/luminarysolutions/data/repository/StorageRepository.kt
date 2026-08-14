package com.example.luminarysolutions.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference

    suspend fun uploadProjectImage(uri: Uri): Result<String> {
        return try {
            val fileName = "projects/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadDocument(uri: Uri, name: String): Result<String> {
        return try {
            val fileName = "documents/${UUID.randomUUID()}_$name"
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
