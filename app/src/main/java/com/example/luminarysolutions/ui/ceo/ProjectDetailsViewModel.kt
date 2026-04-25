package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class ProjectDetailsUiState(
    val project: Project? = null,
    val volunteers: List<com.example.luminarysolutions.data.models.Volunteer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProjectDetailsViewModel : ViewModel() {
    private val repository = ProjectsRepository()
    private val _projectId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProjectDetailsUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(ProjectDetailsUiState(isLoading = false))
            } else {
                combine(
                    repository.getProjectById(id),
                    repository.getVolunteers()
                ) { project, allVolunteers ->
                    if (project != null) {
                        // Filter volunteers who are assigned to this project
                        val projectVolunteers = allVolunteers.filter { project.volunteers.contains(it.id) }
                        ProjectDetailsUiState(
                            project = project,
                            volunteers = if (project.volunteers.isEmpty()) allVolunteers else projectVolunteers, // Fallback to all for demo
                            isLoading = false
                        )
                    } else {
                        ProjectDetailsUiState(isLoading = false, error = "Project not found")
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProjectDetailsUiState()
        )

    fun loadProject(projectId: String) {
        _projectId.value = projectId
    }

    fun toggleTaskStatus(projectId: String, taskId: String, isDone: Boolean) {
        repository.updateTaskStatus(projectId, taskId, isDone) { success ->
            // In a real app, maybe show a snackbar on failure
        }
    }

    fun addTask(projectId: String, title: String, assignedTo: String) {
        val newTask = com.example.luminarysolutions.data.models.Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            assignedTo = assignedTo,
            isDone = false
        )
        repository.addTask(projectId, newTask) { success ->
            // Handle success/failure
        }
    }

    fun assignGroupLeader(projectId: String, leaderId: String) {
        repository.assignGroupLeader(projectId, leaderId) { success ->
            // Handle success/failure
        }
    }
}
