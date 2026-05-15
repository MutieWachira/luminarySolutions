package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.firebase.StorageService
import com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.User
import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DashboardRepository {
    fun getDashboardStats(): Flow<DashboardStats> = FirestoreService.getDashboardStats()
    fun getLumDashStats(year: Int): Flow<lumOverviewDashboardStats> = FirestoreService.getLumDashStats(year)
    fun getOngoingInitiatives(): Flow<List<Project>> = FirestoreService.getProjects().map { projects ->
        projects.take(5) // Just take top 5 for dashboard
    }

    fun getRecentApprovals(): Flow<List<Approval>> = FirestoreService.getApprovals().map { approvals ->
        approvals.take(3)
    }

    fun getRecentDocuments(): Flow<List<Document>> = FirestoreService.getDocuments().map { docs ->
        docs.take(3)
    }

    // Luminary Projects
    fun getLuminaryProjects(): Flow<List<Project>> = FirestoreService.getLuminaryProjects()
    
    fun addLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.addLuminaryProject(project, onComplete)
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteLuminaryProject(projectId, onComplete)
    }

    fun updateLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateLuminaryProject(project, onComplete)
    }

    fun getLuminaryProjectById(projectId: String): Flow<Project?> = FirestoreService.getLuminaryProjectById(projectId)

    suspend fun uploadImage(uri: Uri): String? {
        return StorageService.uploadProjectImage(uri)
    }

    fun getTeamMembers(): Flow<List<User>> = FirestoreService.getTeamMembers()
}

