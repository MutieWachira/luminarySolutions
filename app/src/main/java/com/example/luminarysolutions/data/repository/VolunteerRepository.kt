package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Notification
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Task
import com.example.luminarysolutions.data.models.Volunteer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class VolunteerRepository {
    private val firestoreService = FirestoreService

    fun getVolunteerProfile(volunteerId: String): Flow<Volunteer?> = 
        firestoreService.getVolunteerProfileFlow(volunteerId)

    fun updateProfile(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        firestoreService.updateVolunteer(volunteer, onComplete)
    }

    fun getAvailableCampaigns(): Flow<List<Project>> = firestoreService.getProjects()

    fun getAchievements(): Flow<List<Achievement>> = firestoreService.getAchievements()

    fun getUnlockedAchievements(volunteerId: String): Flow<List<Achievement>> = 
        firestoreService.getUnlockedAchievements(volunteerId)

    fun getNotifications(userId: String): Flow<List<Notification>> = 
        firestoreService.getNotifications(userId)

    fun markNotificationAsRead(notificationId: String) = 
        firestoreService.markNotificationAsRead(notificationId)

    fun getTasks(volunteerId: String): Flow<List<Pair<Task, String>>> = 
        firestoreService.getVolunteerTasks(volunteerId)

    fun getAssignedProjects(userId: String): Flow<List<Project>> =
        firestoreService.getAssignedProjects(listOf(userId))

    fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        firestoreService.updateTaskStatusInProject(projectId, taskId, isDone, onComplete)
    }
}
