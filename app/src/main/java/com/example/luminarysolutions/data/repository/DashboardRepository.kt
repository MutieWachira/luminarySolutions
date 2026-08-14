package com.example.luminarysolutions.data.repository

import android.net.Uri
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.LumOverviewDashboardStats
import com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.Event
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Notification
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Task
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.TeamCulture
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.models.Volunteer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DashboardRepository: Orchestrates data flow between ViewModels and Data Sources (Firestore, Storage).
 * Organized by business unit: Luminary first, then LumiSphere.
 */
@Singleton
class DashboardRepository @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val volunteerRepository: VolunteerRepository,
    private val financeRepository: FinanceRepository,
    private val notificationRepository: NotificationRepository,
    private val storageRepository: StorageRepository,
    private val gamificationRepository: GamificationRepository,
    private val teamRepository: TeamRepository
) {
    // =========================================================================================
    // LUMINARY OPERATIONS
    // =========================================================================================

    fun getLumDashStats(year: Int): Flow<LumOverviewDashboardStats> = financeRepository.getLumDashStats(year)
    
    fun getLuminaryProjects(): Flow<List<Freelance>> = projectRepository.getLuminaryProjects()
    
    fun getLuminaryProjectById(projectId: String): Flow<Freelance?> = projectRepository.getLuminaryProjects().map { it.find { p -> p.id == projectId } }

    suspend fun addLuminaryProject(freelance: Freelance): Result<Unit> {
        return projectRepository.addLuminaryProject(freelance)
    }

    suspend fun updateLuminaryProject(freelance: Freelance): Result<Unit> {
        return projectRepository.updateLuminaryProject(freelance)
    }

    suspend fun deleteLuminaryProject(projectId: String): Result<Unit> {
        return projectRepository.deleteLuminaryProject(projectId)
    }

    suspend fun updateFreelanceTasks(freelanceId: String, tasks: List<Task>): Result<Unit> {
        return projectRepository.updateFreelanceTasks(freelanceId, tasks)
    }

    fun getTeams(): Flow<List<Team>> = teamRepository.getTeamsFlow() 

    fun getTeamsByIds(ids: List<String>): Flow<List<Team>> = teamRepository.getTeamsByIdsFlow(ids)

    suspend fun addTeamMember(team: Team): Result<Unit> {
        return teamRepository.addTeamMember(team)
    }

    suspend fun updateTeamMember(team: Team): Result<Unit> {
        return teamRepository.updateTeamMember(team)
    }

    suspend fun deleteTeamMember(teamId: String): Result<Unit> {
        return teamRepository.deleteTeamMember(teamId)
    }

    fun getTeamCulture(): Flow<TeamCulture> = teamRepository.getTeamCulture()

    suspend fun getTeamsOneShot(): Result<List<Team>> = teamRepository.getTeams()

    suspend fun testSmtp(): Result<String> = teamRepository.testSmtp()

    fun getDocuments(): Flow<List<Document>> = projectRepository.getDocuments()

    suspend fun addDocument(document: Document): Result<Unit> {
        return projectRepository.addDocument(document)
    }

    suspend fun deleteDocument(docId: String): Result<Unit> {
        return projectRepository.deleteDocument(docId)
    }

    // =========================================================================================
    // LUMISPHERE OPERATIONS
    // =========================================================================================

    fun getLumiSphereDashStats(year: Int): Flow<LumiSphereOverviewDashboardStats> = financeRepository.getLumiSphereDashStats(year)

    fun getDashboardStats(): Flow<DashboardStats> = financeRepository.getDashboardStats()

    fun getProjects(): Flow<List<Project>> = projectRepository.getProjects()

    fun getProjectById(projectId: String): Flow<Project?> = projectRepository.getProjectById(projectId)

    fun getOngoingInitiatives(): Flow<List<Project>> = projectRepository.getProjects().map { it.take(5) }

    suspend fun addProject(project: Project): Result<Unit> {
        return projectRepository.addProject(project)
    }

    suspend fun updateProject(project: Project): Result<Unit> {
        return projectRepository.updateProject(project)
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        return projectRepository.deleteProject(projectId)
    }

    fun getDonors(): Flow<List<Donor>> = financeRepository.getDonors()

    fun getDonorsPaginated(lastDocument: com.google.firebase.firestore.DocumentSnapshot?, pageSize: Long): Flow<Pair<List<Donor>, com.google.firebase.firestore.DocumentSnapshot?>> = financeRepository.getDonorsPaginated(lastDocument, pageSize)

    suspend fun addDonor(donor: Donor): Result<Unit> {
        return financeRepository.addDonor(donor)
    }

    suspend fun updateDonor(donor: Donor): Result<Unit> {
        return financeRepository.updateDonor(donor)
    }

    suspend fun deleteDonor(donorId: String): Result<Unit> {
        return financeRepository.deleteDonor(donorId)
    }

    fun getPartners(): Flow<List<Partner>> = projectRepository.getPartners()

    suspend fun addPartner(partner: Partner): Result<Unit> {
        return projectRepository.addPartner(partner)
    }

    fun getApprovals(): Flow<List<Approval>> = financeRepository.getApprovals()

    fun getRecentApprovals(): Flow<List<Approval>> = financeRepository.getApprovals().map { it.take(3) }

    fun getVolunteers(): Flow<List<Volunteer>> = volunteerRepository.getVolunteers()

    fun getVolunteerApplications(): Flow<List<Volunteer>> = volunteerRepository.getVolunteerApplications()

    suspend fun addVolunteerApplication(volunteer: Volunteer): Result<Unit> {
        return volunteerRepository.addVolunteerApplication(volunteer)
    }

    fun getVolunteerProfileFlow(volunteerId: String): Flow<Volunteer?> = volunteerRepository.getVolunteerProfileFlow(volunteerId)

    fun getAssignedProjects(userIds: List<String>): Flow<List<Project>> = projectRepository.getAssignedProjects(userIds)

    fun getUnlockedAchievements(volunteerId: String): Flow<List<Achievement>> = gamificationRepository.getAchievements().map { achievements ->
        // This is a simplification, ideally we filter based on volunteer's unlocked list
        achievements.filter { it.role == "VOLUNTEER" } // Placeholder logic
    }

    suspend fun updateVolunteerStatus(volunteerId: String, status: String): Result<Unit> {
        return volunteerRepository.updateVolunteerStatus(volunteerId, status)
    }

    suspend fun updateVolunteer(volunteer: Volunteer): Result<Unit> {
        return volunteerRepository.updateVolunteer(volunteer)
    }

    suspend fun deleteVolunteer(volunteerId: String): Result<Unit> {
        return volunteerRepository.deleteVolunteer(volunteerId)
    }

    suspend fun removeVolunteerFromProject(projectId: String, volunteerId: String): Result<Unit> {
        return projectRepository.removeVolunteerFromProject(projectId, volunteerId)
    }

    suspend fun assignGroupLeader(projectId: String, leaderId: String): Result<Unit> {
        return projectRepository.assignGroupLeader(projectId, leaderId)
    }

    suspend fun updateProjectTeamMembers(projectId: String, teamMemberIds: List<String>): Result<Unit> {
        return projectRepository.updateProjectTeamMembers(projectId, teamMemberIds)
    }

    suspend fun addTaskToProject(projectId: String, tasks: List<Task>): Result<Unit> {
        return projectRepository.addTasksToProject(projectId, tasks)
    }

    suspend fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, isFreelance: Boolean): Result<Unit> {
        return projectRepository.updateTaskStatus(projectId, taskId, isDone, isFreelance)
    }

    // =========================================================================================
    // EVENTS OPERATIONS
    // =========================================================================================

    fun getEvents(): Flow<List<Event>> = projectRepository.getEvents()

    suspend fun addEvent(event: Event): Result<Unit> {
        return projectRepository.addEvent(event)
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return projectRepository.updateEvent(event)
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return projectRepository.deleteEvent(eventId)
    }

    // =========================================================================================
    // GENERAL / SHARED OPERATIONS
    // =========================================================================================

    fun getTeamMembers(): Flow<List<User>> = userRepository.getTeamMembers()

    fun getUserProfile(userId: String): Flow<User?> = userRepository.getUserProfile(userId)

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = userRepository.getUsersByIds(ids)

    suspend fun uploadFile(uri: Uri, name: String): String? = storageRepository.uploadDocument(uri, name).getOrNull()

    suspend fun uploadImage(uri: Uri): String? = storageRepository.uploadProjectImage(uri).getOrNull()

    suspend fun sendNotification(userId: String, title: String, message: String, type: String): Result<Unit> {
        return notificationRepository.addNotification(
            Notification(
                id = "",
                userId = userId,
                title = title,
                message = message,
                type = type,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    fun getNotifications(userId: String): Flow<List<Notification>> = notificationRepository.getNotifications(userId)

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = notificationRepository.markAsRead(notificationId)
}
