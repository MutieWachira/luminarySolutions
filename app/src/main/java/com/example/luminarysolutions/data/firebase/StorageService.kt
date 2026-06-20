package com.example.luminarysolutions.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object StorageService {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProjectImage(uri: Uri): String? {
        return try {
            Log.d("StorageService", "Starting upload for URI: $uri")
            val fileName = "projects/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(fileName)
            
            val uploadTask = fileRef.putFile(uri)
            
            // Monitor progress or just await
            uploadTask.await()
            
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Log.d("StorageService", "Upload successful. URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("StorageService", "Upload failed for URI: $uri", e)
            null
        }
    }

    suspend fun uploadDocument(uri: Uri, name: String): String? {
        return try {
            val fileName = "documents/${UUID.randomUUID()}_$name"
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(uri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
