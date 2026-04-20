package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Project
import kotlinx.coroutines.flow.Flow

class ProjectsRepository {
    fun getProjects(): Flow<List<Project>> = FirestoreService.getProjects()

    fun getProjectById(projectId: String): Flow<Project?> = FirestoreService.getProjectById(projectId)

    fun addProject(project: Project, onComplete: (Boolean) -> Unit) {
        FirestoreService.addProject(project, onComplete)
    }
}
