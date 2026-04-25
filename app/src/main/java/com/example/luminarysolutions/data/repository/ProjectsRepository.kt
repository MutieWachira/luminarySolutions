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

    suspend fun uploadImage(uri: Uri): String? {
        return StorageService.uploadProjectImage(uri)
    }

    fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateTaskStatus(projectId, taskId, isDone, onComplete)
    }

    fun addTask(projectId: String, task: com.example.luminarysolutions.data.models.Task, onComplete: (Boolean) -> Unit) {
        FirestoreService.addTaskToProject(projectId, task, onComplete)
    }

    fun getVolunteers(): Flow<List<com.example.luminarysolutions.data.models.Volunteer>> = FirestoreService.getVolunteers()

    fun assignGroupLeader(projectId: String, leaderId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.assignGroupLeader(projectId, leaderId, onComplete)
    }
}
