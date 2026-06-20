package com.example.luminarysolutions.data.repository

import android.net.Uri
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.firebase.StorageService
import com.example.luminarysolutions.data.models.Project
import kotlinx.coroutines.flow.Flow

class ProjectsRepository {
    fun getProjects(): Flow<List<Project>> = FirestoreService.getProjects()

    fun getProjectById(projectId: String): Flow<Project?> = FirestoreService.getProjectById(projectId)

    fun addProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.addProject(project, onComplete)
    }

    fun updateProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateProject(project, onComplete)
    }

    fun deleteProject(projectId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteProject(projectId, onComplete)
    }

    suspend fun uploadImage(uri: Uri): String? {
        return StorageService.uploadProjectImage(uri)
    }

    fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateTaskStatus(projectId, taskId, isDone, onComplete = onComplete)
    }

    fun addTask(projectId: String, task: com.example.luminarysolutions.data.models.Task, onComplete: (Boolean) -> Unit) {
        FirestoreService.addTaskToProject(projectId, task, onComplete)
    }

    fun addTasks(projectId: String, tasks: List<com.example.luminarysolutions.data.models.Task>, onComplete: (Boolean) -> Unit) {
        FirestoreService.addTasksToProject(projectId, tasks, onComplete)
    }

    fun updateTasks(projectId: String, tasks: List<com.example.luminarysolutions.data.models.Task>, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateProjectTasks(projectId, tasks, onComplete)
    }

    fun getVolunteers(): Flow<List<com.example.luminarysolutions.data.models.Volunteer>> = FirestoreService.getVolunteers()

    fun getVolunteerApplications(): Flow<List<com.example.luminarysolutions.data.models.Volunteer>> = FirestoreService.getVolunteerApplications()

    fun addVolunteerApplication(volunteer: com.example.luminarysolutions.data.models.Volunteer, onComplete: (Boolean) -> Unit) {
        FirestoreService.addVolunteerApplication(volunteer, onComplete)
    }

    fun updateVolunteerStatus(volunteerId: String, status: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateVolunteerStatus(volunteerId, status, onComplete)
    }

    fun updateVolunteer(volunteer: com.example.luminarysolutions.data.models.Volunteer, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateVolunteer(volunteer, onComplete)
    }

    fun deleteVolunteer(volunteerId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteVolunteer(volunteerId, onComplete)
    }

    fun removeVolunteerFromProject(projectId: String, volunteerId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.removeVolunteerFromProject(projectId, volunteerId, onComplete)
    }

    fun getVolunteerById(volunteerId: String, onComplete: (com.example.luminarysolutions.data.models.Volunteer?) -> Unit) {
        FirestoreService.getVolunteerById(volunteerId, onComplete)
    }

    fun getTeamMembers(): Flow<List<com.example.luminarysolutions.data.models.User>> = FirestoreService.getTeamMembers()

    /**
     * Checks if a user already has an account.
     */
    fun checkUserExists(email: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.checkUserExistsByEmail(email, onComplete)
    }

    fun createVolunteerUser(volunteer: com.example.luminarysolutions.data.models.Volunteer, onComplete: (Boolean) -> Unit) {
        FirestoreService.createVolunteerUser(volunteer, onComplete)
    }

    fun addDonation(
        donorId: String?, 
        amount: Int, 
        method: String, 
        projectId: String? = null,
        details: Map<String, String> = emptyMap(), 
        onComplete: (Boolean) -> Unit
    ) {
        FirestoreService.addDonation(donorId, amount, method, projectId, details, onComplete)
    }

    fun assignGroupLeader(projectId: String, leaderId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.assignGroupLeader(projectId, leaderId, onComplete)
    }

    fun updateProjectTeamMembers(projectId: String, teamMemberIds: List<String>, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateProjectTeamMembers(projectId, teamMemberIds, onComplete)
    }

    /**
     * Updates the FCM registration token for push notifications.
     */
    fun updateFcmToken(userId: String, token: String) {
        FirestoreService.updateFcmToken(userId, token) {
            // Log or handle completion if necessary
        }
    }
}
