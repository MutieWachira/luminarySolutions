package com.example.luminarysolutions.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object StorageService {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProjectImage(uri: Uri): String? {
        return try {
            val fileName = "projects/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(uri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
