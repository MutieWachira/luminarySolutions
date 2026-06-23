package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val volunteerApplications: List<com.example.luminarysolutions.data.models.Volunteer> = emptyList(),
    val teamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

class ProjectDetailsViewModel : ViewModel() {
    private val repository = ProjectsRepository()
    private val _projectId = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProjectDetailsUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(ProjectDetailsUiState(isLoading = false))
            } else {
                combine(
                    repository.getProjectById(id),
                    repository.getVolunteers(),
                    repository.getVolunteerApplications(),
                    repository.getTeamMembers(),
                    _isSaving
                ) { project, allVolunteers, allApplications, allTeamMembers, saving ->
                    if (project != null) {
                        // Filter volunteers who are assigned to this project
                        // We check both the document ID and match by email with users who are assigned
                        // This handles cases where project.volunteers contains Auth UIDs but Volunteer objects use random doc IDs
                        val projectVolunteers = allVolunteers.filter { volunteer ->
                            project.volunteers.contains(volunteer.id) || 
                            allTeamMembers.any { user -> 
                                user.email == volunteer.email && 
                                project.volunteers.contains(user.id) &&
                                user.role == com.example.luminarysolutions.ui.auth.UserRole.VOLUNTEER 
                            }
                        }
                        // Filter applications for this project
                        val projectApplications = allApplications.filter { it.projectIds.contains(project.id) }

                        ProjectDetailsUiState(
                            project = project,
                            volunteers = projectVolunteers,
                            volunteerApplications = projectApplications,
                            teamMembers = allTeamMembers,
                            isLoading = false,
                            isSaving = saving
                        )
                    } else {
                        ProjectDetailsUiState(isLoading = false, isSaving = saving, error = "Project not found")
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

    fun addTask(
        projectId: String, 
        title: String, 
        description: String,
        assignedToIds: List<String>,
        assignedToNames: List<String>,
        assignedById: String,
        assigneeType: com.example.luminarysolutions.data.models.AssigneeType,
        deadline: Long
    ) {
        val currentState = uiState.value
        val project = currentState.project ?: return
        _isSaving.value = true

        val newTask = com.example.luminarysolutions.data.models.Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            assignedToIds = assignedToIds,
            assignedToNames = assignedToNames,
            assignedById = assignedById,
            assigneeType = assigneeType,
            deadline = deadline,
            isDone = false
        )
        repository.addTask(projectId, newTask) { success ->
            _isSaving.value = false
            if (success) {
                assignedToIds.forEach { userId ->
                    com.example.luminarysolutions.data.firebase.FirestoreService.notifyUser(
                        userId = userId,
                        title = "New Task: ${project.name}",
                        message = "You have been assigned: $title",
                        type = "TASK"
                    )
                }
            }
        }
    }

    fun updateTask(projectId: String, updatedTask: com.example.luminarysolutions.data.models.Task) {
        val currentProject = uiState.value.project ?: return
        _isSaving.value = true
        val updatedTasks = currentProject.tasks.map {
            if (it.id == updatedTask.id) updatedTask else it
        }
        repository.updateTasks(projectId, updatedTasks) { 
            _isSaving.value = false
        }
    }

    fun deleteTask(projectId: String, taskId: String) {
        val currentProject = uiState.value.project ?: return
        val updatedTasks = currentProject.tasks.filter { it.id != taskId }
        repository.updateTasks(projectId, updatedTasks) { }
    }

    fun assignGroupLeader(projectId: String, leaderId: String) {
        repository.assignGroupLeader(projectId, leaderId) { success ->
            // Handle success/failure
        }
    }

    fun updateTeamMembers(projectId: String, teamMemberIds: List<String>) {
        repository.updateProjectTeamMembers(projectId, teamMemberIds) { success ->
            // Handle success/failure
        }
    }

    fun approveVolunteer(volunteerId: String) {
        // High-level: Update status to 'Approved'. 
        // A backend Cloud Function (onVolunteerStatusChange) will automatically:
        // 1. Create the user record in the 'users' collection.
        // 2. Send the approval email with the temporary password.
        repository.updateVolunteerStatus(volunteerId, "Approved") { success ->
            if (!success) {
                android.util.Log.e("ProjectDetailsViewModel", "Failed to approve volunteer. Permission Denied?")
            }
        }
    }

    fun rejectVolunteer(volunteerId: String) {
        // High-level: Update status to 'Rejected'.
        // The backend trigger handles sending the rejection email.
        repository.updateVolunteerStatus(volunteerId, "Rejected") { success ->
            if (!success) {
                android.util.Log.e("ProjectDetailsViewModel", "Failed to reject volunteer. Permission Denied?")
            }
        }
    }

    /**
     * Deletes a volunteer from the system and removes them from the project.
     * High-level operation for management.
     */
    fun deleteVolunteer(projectId: String, volunteerId: String) {
        repository.deleteVolunteer(volunteerId) { success ->
            if (success) {
                repository.removeVolunteerFromProject(projectId, volunteerId) {
                    // Handled via Flow
                }
            }
        }
    }

    /**
     * Updates volunteer details.
     */
    fun updateVolunteer(volunteer: com.example.luminarysolutions.data.models.Volunteer) {
        repository.updateVolunteer(volunteer) {
            // Handled via Flow
        }
    }
}
