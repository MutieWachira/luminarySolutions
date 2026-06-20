package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.DashboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FreelanceDetailsUiState(
    val freelance: Freelance? = null,
    val assignedTeam: List<Team> = emptyList(),
    val applicants: List<User> = emptyList(),
    val allTeamMembers: List<Team> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FreelanceDetailsViewModel : ViewModel() {
    private val repository = DashboardRepository()
    private val _projectId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FreelanceDetailsUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(FreelanceDetailsUiState(isLoading = false))
            } else {
                repository.getLuminaryProjectById(id).flatMapLatest { freelance ->
                    if (freelance != null) {
                        combine(
                            repository.getTeamsByIds(freelance.teamIds),
                            repository.getUsersByIds(freelance.clientIds),
                            repository.getTeams()
                        ) { assigned, applicants, allMembers ->
                            FreelanceDetailsUiState(
                                freelance = freelance,
                                assignedTeam = assigned,
                                applicants = applicants,
                                allTeamMembers = allMembers,
                                isLoading = false
                            )
                        }
                    } else {
                        flowOf(FreelanceDetailsUiState(isLoading = false, error = "Service not found"))
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FreelanceDetailsUiState()
        )

    fun loadProject(projectId: String) {
        _projectId.value = projectId
    }

    fun updateFreelance(freelance: Freelance) {
        repository.updateLuminaryProject(freelance) { success ->
            // Handle result
        }
    }

    fun assignToTeam(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (!currentFreelance.teamIds.contains(userId)) {
            val updatedTeam = currentFreelance.teamIds + userId
            // Also remove from applicants if they were there
            val updatedApplicants = currentFreelance.clientIds - userId
            updateFreelance(currentFreelance.copy(teamIds = updatedTeam, clientIds = updatedApplicants))
        }
    }

    fun removeFromTeam(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (currentFreelance.teamIds.contains(userId)) {
            val updatedTeam = currentFreelance.teamIds - userId
            updateFreelance(currentFreelance.copy(teamIds = updatedTeam))
        }
    }
    
    fun rejectApplicant(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (currentFreelance.clientIds.contains(userId)) {
            val updatedApplicants = currentFreelance.clientIds - userId
            updateFreelance(currentFreelance.copy(clientIds = updatedApplicants))
        }
    }

    fun addTask(
        title: String, 
        description: String,
        assignedToIds: List<String>,
        assignedToNames: List<String>,
        deadline: Long,
        assignedById: String
    ) {
        val currentFreelance = uiState.value.freelance ?: return
        val newTask = com.example.luminarysolutions.data.models.Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            assignedToIds = assignedToIds,
            assignedToNames = assignedToNames,
            assignedById = assignedById,
            assigneeType = com.example.luminarysolutions.data.models.AssigneeType.TEAM,
            deadline = deadline,
            isDone = false
        )
        val updatedTasks = currentFreelance.tasks + newTask
        repository.updateFreelanceTasks(currentFreelance.id, updatedTasks) { success ->
             if (success) {
                assignedToIds.forEach { userId ->
                    com.example.luminarysolutions.data.firebase.FirestoreService.notifyUser(
                        userId = userId,
                        title = "New Task: ${currentFreelance.name}",
                        message = "You have been assigned: $title. Deadline: ${java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(deadline))}",
                        type = "TASK"
                    )
                }
            }
        }
    }

    fun updateTask(updatedTask: com.example.luminarysolutions.data.models.Task) {
        val currentFreelance = uiState.value.freelance ?: return
        val updatedTasks = currentFreelance.tasks.map {
            if (it.id == updatedTask.id) updatedTask else it
        }
        repository.updateFreelanceTasks(currentFreelance.id, updatedTasks) { }
    }

    fun deleteTask(taskId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        val updatedTasks = currentFreelance.tasks.filter { it.id != taskId }
        repository.updateFreelanceTasks(currentFreelance.id, updatedTasks) { }
    }

    fun toggleTaskStatus(taskId: String, isDone: Boolean) {
        val currentFreelance = uiState.value.freelance ?: return
        val updatedTasks = currentFreelance.tasks.map {
            if (it.id == taskId) it.copy(isDone = isDone) else it
        }
        repository.updateFreelanceTasks(currentFreelance.id, updatedTasks) {
            // Updated
        }
    }
}
