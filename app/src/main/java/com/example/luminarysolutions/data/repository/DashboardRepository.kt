package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.firebase.StorageService
import com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Freelance
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
    fun getLuminaryProjects(): Flow<List<Freelance>> = FirestoreService.getLuminaryProjects()
    
    fun addLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        FirestoreService.addLuminaryProject(freelance, onComplete)
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteLuminaryProject(projectId, onComplete)
    }

    fun updateLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateLuminaryProject(freelance, onComplete)
    }

    fun getLuminaryProjectById(projectId: String): Flow<Freelance?> = FirestoreService.getLuminaryProjectById(projectId)

    suspend fun uploadImage(uri: Uri): String? {
        return StorageService.uploadProjectImage(uri)
    }

    fun getTeamMembers(): Flow<List<User>> = FirestoreService.getTeamMembers()

    fun getTeams(): Flow<List<com.example.luminarysolutions.data.models.Team>> = FirestoreService.getTeams()

    fun addTeamMember(team: com.example.luminarysolutions.data.models.Team, onComplete: (Boolean) -> Unit) {
        FirestoreService.addTeamMember(team, onComplete)
    }

    fun updateTeamMember(team: com.example.luminarysolutions.data.models.Team, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateTeamMember(team, onComplete)
    }

    fun deleteTeamMember(teamId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteTeamMember(teamId, onComplete)
    }

    fun getTeamCulture(): Flow<com.example.luminarysolutions.data.models.TeamCulture> = FirestoreService.getTeamCulture()

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = FirestoreService.getUsersByIds(ids)
}

