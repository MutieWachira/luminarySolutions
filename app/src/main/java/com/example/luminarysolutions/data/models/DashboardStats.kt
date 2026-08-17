package com.example.luminarysolutions.data.models

/**
 * DashboardStats: Encapsulates key metrics for the client dashboard.
 */
data class DashboardStats(
    val activeProjectsCount: Int = 0,
    val pendingRequestsCount: Int = 0,
    val completedProjectsCount: Int = 0,
    val totalInvested: Double = 0.0
)
