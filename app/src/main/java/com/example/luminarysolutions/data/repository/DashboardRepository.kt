package com.example.luminarysolutions.data.repository

import android.net.Uri
import com.example.luminarysolutions.data.firebase.*
import com.example.luminarysolutions.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DashboardRepository: Orchestrates data flow between ViewModels and Data Sources (Firestore, Storage).
 * Organized by business unit: Luminary first, then LumiSphere.
 */
class DashboardRepository {
    private val teamRepository = TeamRepository()

    // =========================================================================================
    // LUMINARY OPERATIONS
    // =========================================================================================

    fun getLumDashStats(year: Int): Flow<lumOverviewDashboardStats> = FirestoreService.getLumDashStats(year)
    
    fun getLuminaryProjects(): Flow<List<Freelance>> = FirestoreService.getLuminaryProjects()
    
    fun getLuminaryProjectById(projectId: String): Flow<Freelance?> = FirestoreService.getLuminaryProjectById(projectId)

    fun addLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        FirestoreService.addLuminaryProject(freelance, onComplete)
    }

    fun updateLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateLuminaryProject(freelance, onComplete)
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteLuminaryProject(projectId, onComplete)
    }

    fun updateFreelanceTasks(freelanceId: String, tasks: List<com.example.luminarysolutions.data.models.Task>, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateFreelanceTasks(freelanceId, tasks, onComplete)
    }

    fun getTeams(): Flow<List<Team>> = FirestoreService.getTeams()

    fun getTeamsByIds(ids: List<String>): Flow<List<Team>> = FirestoreService.getTeamsByIds(ids)

    suspend fun addTeamMember(team: Team): Result<Unit> {
        return teamRepository.addTeamMember(team)
    }

    fun updateTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateTeamMember(team, onComplete)
    }

    suspend fun deleteTeamMember(teamId: String): Result<Unit> {
        return teamRepository.deleteTeamMember(teamId)
    }

    fun getTeamCulture(): Flow<TeamCulture> = FirestoreService.getTeamCulture()

    fun getDocuments(): Flow<List<Document>> = FirestoreService.getDocuments()

    fun addDocument(document: Document, onComplete: (Boolean) -> Unit) {
        FirestoreService.addDocument(document, onComplete)
    }

    fun deleteDocument(docId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteDocument(docId, onComplete)
    }

    // =========================================================================================
    // LUMISPHERE OPERATIONS
    // =========================================================================================

    fun getLumiSphereDashStats(year: Int): Flow<lumiSphereOverviewDashboardStats> = FirestoreService.getLumiSphereDashStats(year)

    fun getDashboardStats(): Flow<DashboardStats> = FirestoreService.getDashboardStats()

    fun getProjects(): Flow<List<Project>> = FirestoreService.getProjects()

    fun getOngoingInitiatives(): Flow<List<Project>> = FirestoreService.getProjects().map { it.take(5) }

    fun addProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.addProject(project, onComplete)
    }

    fun updateProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateProject(project, onComplete)
    }

    fun deleteProject(projectId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteProject(projectId, onComplete)
    }

    fun getDonors(): Flow<List<Donor>> = FirestoreService.getDonors()

    fun addDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        FirestoreService.addDonor(donor, onComplete)
    }

    fun getPartners(): Flow<List<Partner>> = FirestoreService.getPartners()

    fun addPartner(partner: Partner, onComplete: (Boolean) -> Unit) {
        FirestoreService.addPartner(partner, onComplete)
    }

    fun getApprovals(): Flow<List<Approval>> = FirestoreService.getApprovals()

    fun getRecentApprovals(): Flow<List<Approval>> = FirestoreService.getApprovals().map { it.take(3) }

    fun getVolunteers(): Flow<List<Volunteer>> = FirestoreService.getVolunteers()

    fun getVolunteerApplications(): Flow<List<Volunteer>> = FirestoreService.getVolunteerApplications()

    fun updateVolunteerStatus(volunteerId: String, status: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateVolunteerStatus(volunteerId, status, onComplete)
    }

    fun updateVolunteer(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateVolunteer(volunteer, onComplete)
    }

    fun deleteVolunteer(volunteerId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteVolunteer(volunteerId, onComplete)
    }

    // =========================================================================================
    // EVENTS OPERATIONS
    // =========================================================================================

    fun getEvents(): Flow<List<Event>> = FirestoreService.getEvents()

    fun addEvent(event: Event, onComplete: (Boolean) -> Unit) {
        FirestoreService.addEvent(event, onComplete)
    }

    fun updateEvent(event: Event, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateEvent(event, onComplete)
    }

    fun deleteEvent(eventId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteEvent(eventId, onComplete)
    }

    // =========================================================================================
    // GENERAL / SHARED OPERATIONS
    // =========================================================================================

    fun getTeamMembers(): Flow<List<User>> = FirestoreService.getTeamMembers()

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = FirestoreService.getUsersByIds(ids)

    suspend fun uploadFile(uri: Uri, name: String): String? {
        return StorageService.uploadDocument(uri, name)
    }

    suspend fun uploadImage(uri: Uri): String? {
        return StorageService.uploadProjectImage(uri)
    }
}
