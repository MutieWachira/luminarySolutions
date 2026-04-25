package com.example.luminarysolutions.data.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService{
    private val db = FirebaseFirestore.getInstance()
    private const val ORG_ID = "lumisphere"

    fun getDashboardStats(onResult: (DashboardStats) -> Unit){
        val orgRef = db.collection(ORG_ID)
        var projects = 0
        var donors = 0
        var expenses = 0

        orgRef.document("projects").collection("items")
            .addSnapshotListener{ snapshot, _ ->
                projects = snapshot?.size() ?: 0
                onResult(DashboardStats(projects, donors, expenses))
            }
        // Assuming donors and expenses follow a similar pattern based on your "lumisphere / projects" description
        orgRef.document("donors").collection("items")
            .addSnapshotListener{ snapshot, _ ->
                donors = snapshot?.size() ?: 0
                onResult(DashboardStats(projects, donors, expenses))
            }
        orgRef.document("expenses").collection("items")
            .addSnapshotListener{ snapshot, _ ->
                expenses = snapshot?.size() ?: 0
                onResult(DashboardStats(projects, donors, expenses))
            }
    }
}