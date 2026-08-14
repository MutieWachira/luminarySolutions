package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailsUiState(
    val project: Project? = null,
    val volunteers: List<com.example.luminarysolutions.data.models.Volunteer> = emptyList(),
    val volunteerApplications: List<com.example.luminarysolutions.data.models.Volunteer> = emptyList(),
    val teamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
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
                ) { project: Project?, allVolunteers: List<com.example.luminarysolutions.data.models.Volunteer>, allApplications: List<com.example.luminarysolutions.data.models.Volunteer>, allTeamMembers: List<com.example.luminarysolutions.data.models.User>, saving: Boolean ->
                    if (project != null) {
                        // Filter volunteers who are assigned to this project
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
        viewModelScope.launch {
            repository.updateTaskStatus(projectId, taskId, isDone, false).onFailure { error ->
                // Handle failure
            }
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
        viewModelScope.launch {
            repository.addTaskToProject(projectId, listOf(newTask)).onSuccess {
                _isSaving.value = false
                assignedToIds.forEach { userId ->
                    repository.sendNotification(
                        userId = userId,
                        title = "New Task: ${project.name}",
                        message = "You have been assigned: $title",
                        type = "TASK"
                    )
                }
            }.onFailure {
                _isSaving.value = false
            }
        }
    }

    fun updateTask(projectId: String, updatedTask: com.example.luminarysolutions.data.models.Task) {
        val currentProject = uiState.value.project ?: return
        _isSaving.value = true
        val updatedTasks = currentProject.tasks.map {
            if (it.id == updatedTask.id) updatedTask else it
        }
        viewModelScope.launch {
            repository.updateProject(currentProject.copy(tasks = updatedTasks)).onSuccess {
                _isSaving.value = false
            }.onFailure {
                _isSaving.value = false
            }
        }
    }

    fun deleteTask(projectId: String, taskId: String) {
        val currentProject = uiState.value.project ?: return
        val updatedTasks = currentProject.tasks.filter { it.id != taskId }
        viewModelScope.launch {
            repository.updateProject(currentProject.copy(tasks = updatedTasks))
        }
    }

    fun assignGroupLeader(projectId: String, leaderId: String) {
        viewModelScope.launch {
            repository.assignGroupLeader(projectId, leaderId)
        }
    }

    fun updateTeamMembers(projectId: String, teamMemberIds: List<String>) {
        viewModelScope.launch {
            repository.updateProjectTeamMembers(projectId, teamMemberIds)
        }
    }

    fun approveVolunteer(volunteerId: String) {
        viewModelScope.launch {
            repository.updateVolunteerStatus(volunteerId, "Approved")
        }
    }

    fun rejectVolunteer(volunteerId: String) {
        viewModelScope.launch {
            repository.updateVolunteerStatus(volunteerId, "Rejected")
        }
    }

    fun deleteVolunteer(projectId: String, volunteerId: String) {
        viewModelScope.launch {
            repository.deleteVolunteer(volunteerId).onSuccess {
                repository.removeVolunteerFromProject(projectId, volunteerId)
            }
        }
    }

    fun updateVolunteer(volunteer: com.example.luminarysolutions.data.models.Volunteer) {
        viewModelScope.launch {
            repository.updateVolunteer(volunteer)
        }
    }
}
