package com.example.luminarysolutions.ui.itadmin

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ITAdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val users = mutableStateListOf<UserItem>()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users.clear()
                for (doc in snapshot.documents) {
                    val user = UserItem(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "PUBLIC",
                        enabled = doc.getBoolean("enabled") ?: true
                    )
                    users.add(user)
                }
            }
    }
}
