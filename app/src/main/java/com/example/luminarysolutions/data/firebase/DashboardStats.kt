package com.example.luminarysolutions.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



//main dashboard stats
data class DashboardStats(
    val totalProjects: Int = 0,
    val totalDonors: Int = 0,
    val totalExpenses: Int = 0,
    val totalPartners: Int = 0,
    val totalRevenue: Int = 0
)

//luminary overview dashboard stats
data class lumOverviewDashboardStats(
    val totalRevenue: Int = 0,
    val totalExpenses: Int = 0,
    val totalProfit: Int = 0,
    val totalProjects: Int = 0,
    val totalActiveClient: Int = 0,
    val monthlyStats: List<MonthlyFinancialStats> = emptyList()
)

//Luminary overview dashboard financial overview chart
data class MonthlyFinancialStats(
    val month: String,
    val revenue: Int,
    val expenses: Int
)

//luminary financial dashboard stats