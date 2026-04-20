package com.example.luminarysolutions.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



data class DashboardStats(
    val totalProjects: Int = 0,
    val totalDonors: Int = 0,
    val totalExpenses: Int = 0,
    val totalPartners: Int = 0
)