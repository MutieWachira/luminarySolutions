package com.example.luminarysolutions.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Notification
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Task
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.repository.DashboardRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced UI State for the Team Dashboard.
 */
data class TeamDashboardUiState(
    val userProfile: Team? = null,
    val assignedFreelance: List<Freelance> = emptyList(), // Luminary
    val assignedProjects: List<Project> = emptyList(),   // LumiSphere
    val userTasks: List<TaskWrapper> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * TaskWrapper: Links a task to its parent project/freelance for context.
 */
data class TaskWrapper(
    val task: Task,
    val projectId: String,
    val projectName: String,
    val isFreelance: Boolean = false
)

/**
 * TeamDashboardViewModel: Manages the business logic for the Team Dashboard.
 */
@HiltViewModel
class TeamDashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: DashboardRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TeamDashboardUiState())
    val uiState: StateFlow<TeamDashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    /**
     * Observes dashboard data in real-time.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboardData() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: return
        val authUid = currentUser.uid
        
        _uiState.update { it.copy(isLoading = true) }

        // Fetch team profile based on email
        viewModelScope.launch {
            repository.getTeamMembers().map { members ->
                members.find { it.email == email }?.let { user ->
                    // Map User to Team for dashboard
                    Team(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        imageUrl = user.profileImageUrl,
                        phone = user.phoneNumber,
                        bio = user.bio,
                        role = user.role,
                        enabled = user.enabled,
                        isTwoFactorEnabled = user.isTwoFactorEnabled
                    )
                }
            }.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }

        viewModelScope.launch {
            _uiState.map { it.userProfile }
                .filterNotNull()
                .flatMapLatest { profile ->
                    val identifiers = listOfNotNull(profile.id, authUid).distinct()
                    
                    combine(
                        repository.getLuminaryProjects(),
                        repository.getAssignedProjects(identifiers),
                        repository.getNotifications(authUid)
                    ) { freelance: List<Freelance>, projects: List<Project>, notifications: List<Notification> ->
                        // Filter assigned freelance projects
                        val assignedFreelance = freelance.filter { f ->
                            identifiers.any { id -> f.teamIds.contains(id) || f.tasks.any { t -> t.assignedToIds.contains(id) } }
                        }
                        DataTriple(assignedFreelance, projects, notifications, identifiers, profile.name)
                    }
                }
                .collect { data ->
                    val identifiers = data.identifiers
                    val profileName = data.profileName
                    
                    // Collect tasks from LumiSphere projects
                    val projectTasks = data.projects.flatMap { project ->
                        project.tasks.filter { task ->
                            identifiers.any { id -> task.assignedToIds.contains(id) } || 
                            task.assignedToNames.contains(profileName)
                        }.map { TaskWrapper(it, project.id, project.name, false) }
                    }

                    // Collect tasks from Luminary freelance projects
                    val freelanceTasks = data.freelance.flatMap { f ->
                        f.tasks.filter { task ->
                            identifiers.any { id -> task.assignedToIds.contains(id) } || 
                            task.assignedToNames.contains(profileName)
                        }.map { TaskWrapper(it, f.id, f.name, true) }
                    }

                    // Merge and sort all tasks by deadline (Priority first)
                    val allTasks = (projectTasks + freelanceTasks)
                        .sortedWith(compareBy({ it.task.isDone }, { it.task.deadline }))
                    
                    _uiState.update { it.copy(
                        assignedFreelance = data.freelance,
                        assignedProjects = data.projects,
                        userTasks = allTasks,
                        notifications = data.notifications,
                        isLoading = false,
                        isRefreshing = false
                    ) }
                }
        }
    }

    private data class DataTriple(
        val freelance: List<Freelance>,
        val projects: List<Project>,
        val notifications: List<Notification>,
        val identifiers: List<String>,
        val profileName: String
    )

    /**
     * Toggles the completion status of a task.
     */
    fun toggleTaskCompletion(wrapper: TaskWrapper) {
        val originalTasks = _uiState.value.userTasks
        val updatedTasks = originalTasks.map {
            if (it.task.id == wrapper.task.id && it.projectId == wrapper.projectId) {
                it.copy(task = it.task.copy(isDone = !it.task.isDone))
            } else it
        }
        
        // Optimistic Update
        _uiState.update { it.copy(userTasks = updatedTasks) }

        viewModelScope.launch {
            repository.updateTaskStatus(
                projectId = wrapper.projectId, 
                taskId = wrapper.task.id, 
                isDone = !wrapper.task.isDone,
                isFreelance = wrapper.isFreelance
            ).onFailure {
                // Rollback on failure
                _uiState.update { it.copy(userTasks = originalTasks, error = "Sync Failed: Please check your connection") }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        observeDashboardData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
