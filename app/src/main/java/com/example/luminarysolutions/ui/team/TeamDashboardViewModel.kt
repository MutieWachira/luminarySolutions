package com.example.luminarysolutions.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Enhanced UI State for the Team Dashboard.
 * Includes explicit support for Luminary and LumiSphere projects.
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
 * Follows MVVM architecture and industry standards for real-time data handling.
 */
class TeamDashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(TeamDashboardUiState())
    val uiState: StateFlow<TeamDashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    /**
     * Observes dashboard data in real-time.
     * Combines multiple Firestore streams and filters data based on user identity.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboardData() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: return
        val authUid = currentUser.uid
        
        _uiState.update { it.copy(isLoading = true) }

        // Fetch team profile based on email
        val profileFlow = FirestoreService.getTeamProfileByEmail(email)

        viewModelScope.launch {
            profileFlow.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }

        viewModelScope.launch {
            profileFlow
                .filterNotNull()
                .flatMapLatest { profile ->
                    // Use both profile ID (internal) and Auth UID (Firebase) for robustness
                    val identifiers = listOfNotNull(profile.id, authUid).distinct()
                    
                    combine(
                        FirestoreService.getAssignedFreelanceProjects(identifiers),
                        FirestoreService.getAssignedProjects(identifiers),
                        FirestoreService.getNotifications(authUid)
                    ) { freelance, projects, notifications ->
                        DataTriple(freelance, projects, notifications, identifiers, profile.name)
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

    /**
     * Helper class to hold combined flow data.
     */
    private data class DataTriple(
        val freelance: List<Freelance>,
        val projects: List<Project>,
        val notifications: List<Notification>,
        val identifiers: List<String>,
        val profileName: String
    )

    /**
     * Toggles the completion status of a task.
     * Uses optimistic UI updates for a snappy professional feel.
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
            try {
                FirestoreService.updateTaskStatus(
                    projectId = wrapper.projectId, 
                    taskId = wrapper.task.id, 
                    isDone = !wrapper.task.isDone,
                    isFreelanceHint = wrapper.isFreelance
                ) { success ->
                    if (!success) {
                        // Rollback on failure
                        _uiState.update { it.copy(userTasks = originalTasks, error = "Sync Failed: Please check your connection") }
                    }
                }
            } catch (e: Exception) {
                // Rollback on crash
                _uiState.update { it.copy(userTasks = originalTasks, error = "Operation failed: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Marks a notification as read.
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            FirestoreService.markNotificationAsRead(notificationId)
        }
    }

    /**
     * Triggers a manual refresh of the dashboard data.
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        observeDashboardData()
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
