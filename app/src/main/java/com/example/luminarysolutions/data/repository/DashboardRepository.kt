package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DashboardRepository {
    fun getDashboardStats(): Flow<DashboardStats> = FirestoreService.getDashboardStats()
    
    fun getOngoingInitiatives(): Flow<List<Project>> = FirestoreService.getProjects().map { projects ->
        projects.take(5) // Just take top 5 for dashboard
    }

    fun getRecentApprovals(): Flow<List<Approval>> = FirestoreService.getApprovals().map { approvals ->
        approvals.take(3)
    }
}
