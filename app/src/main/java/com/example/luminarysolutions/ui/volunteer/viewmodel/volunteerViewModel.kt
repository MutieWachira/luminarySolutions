package com.example.luminarysolutions.ui.volunteer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Notification
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Task
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.VolunteerRepository
import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.models.VolunteerEventUi
import com.example.luminarysolutions.ui.volunteer.models.VolunteerTaskUi
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VolunteerViewModel : ViewModel() {
    private val repository = VolunteerRepository()
    private val auth = FirebaseAuth.getInstance()

    // Profile State
    private val _profile = MutableStateFlow<Volunteer?>(null)
    val profile: StateFlow<Volunteer?> = _profile.asStateFlow()

    // Campaigns/Explore State
    private val _campaigns = MutableStateFlow<List<Project>>(emptyList())
    val campaigns: StateFlow<List<Project>> = _campaigns.asStateFlow()

    // Derived flows for better architecture and real-time filtering
    val myCampaigns: StateFlow<List<Project>> = combine(_campaigns, _profile) { allCampaigns, volunteer ->
        val volunteerId = volunteer?.id ?: auth.currentUser?.uid ?: ""
        allCampaigns.filter { project ->
            project.volunteers.contains(volunteerId) || (volunteer?.projectIds?.contains(project.id) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exploreCampaigns: StateFlow<List<Project>> = combine(_campaigns, _profile) { allCampaigns, volunteer ->
        val volunteerId = volunteer?.id ?: auth.currentUser?.uid ?: ""
        allCampaigns.filter { project ->
            !project.volunteers.contains(volunteerId) && (volunteer?.projectIds?.contains(project.id) != true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Achievements State
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val unlockedAchievements: StateFlow<List<Achievement>> = _unlockedAchievements.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Tasks State
    var tasks by mutableStateOf<List<VolunteerTaskUi>>(emptyList())
        private set

    var events by mutableStateOf<List<VolunteerEventUi>>(emptyList())
        private set

    private var isLoaded = false

    fun load(volunteerId: String) {
        if (isLoaded) return
        isLoaded = true

        val currentUserId = auth.currentUser?.uid ?: volunteerId
        
        // Ensure achievements are seeded if collection is empty
        com.example.luminarysolutions.data.firebase.FirestoreService.seedAchievements()

        viewModelScope.launch {
            repository.getVolunteerProfile(currentUserId).collect {
                _profile.value = it
            }
        }
        viewModelScope.launch {
            repository.getAvailableCampaigns().collect {
                _campaigns.value = it
            }
        }
        viewModelScope.launch {
            repository.getAchievements().collect {
                android.util.Log.d("VolunteerViewModel", "Loaded ${it.size} achievements")
                _achievements.value = it
            }
        }
        viewModelScope.launch {
            repository.getUnlockedAchievements(currentUserId).collect {
                _unlockedAchievements.value = it
            }
        }
        viewModelScope.launch {
            repository.getNotifications(currentUserId).collect {
                _notifications.value = it
            }
        }
        viewModelScope.launch {
            repository.getTasks(currentUserId).collect { firestoreTasksWithId ->
                tasks = firestoreTasksWithId.map { (task, projectId) -> 
                    mapToUiTask(task, projectId) 
                }
            }
        }
        viewModelScope.launch {
            repository.getAssignedProjects(currentUserId).collect { assignedProjects ->
                events = assignedProjects.map { mapToUiEvent(it) }
            }
        }
    }

    private fun mapToUiEvent(project: Project): VolunteerEventUi {
        return VolunteerEventUi(
            id = project.id,
            name = project.name,
            date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(project.startDate)),
            venue = project.location,
            notes = project.description.take(100) + if (project.description.length > 100) "..." else ""
        )
    }

    private fun mapToUiTask(task: Task, projectId: String): VolunteerTaskUi {
        return VolunteerTaskUi(
            id = task.id,
            projectId = projectId,
            title = task.title,
            description = task.description,
            status = if (task.isDone) TaskStatus.DONE else TaskStatus.ASSIGNED,
            dueDate = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.deadline)),
            location = "Project Task",
            lastUpdate = "Created ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.createdAt))}"
        )
    }

    fun updateProfile(volunteer: Volunteer) {
        viewModelScope.launch {
            repository.updateProfile(volunteer) { success ->
                if (success) _profile.value = volunteer
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            viewModelScope.launch {
                try {
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword).await()
                    onResult(true, null)
                } catch (e: Exception) {
                    onResult(false, e.localizedMessage)
                }
            }
        } else {
            onResult(false, "User not authenticated")
        }
    }

    fun markAsRead(notificationId: String) {
        repository.markNotificationAsRead(notificationId)
    }

    fun updateSettings(notificationsEnabled: Boolean, darkModeEnabled: Boolean) {
        _profile.value?.let { current ->
            val updated = current.copy(
                notificationsEnabled = notificationsEnabled,
                darkModeEnabled = darkModeEnabled
            )
            repository.updateProfile(updated) { success ->
                if (success) _profile.value = updated
            }
        }
    }

    fun getTask(taskId: String) = tasks.firstOrNull { it.id == taskId }

    fun setStatus(taskId: String, status: TaskStatus, onComplete: (Boolean) -> Unit = {}) {
        val task = tasks.firstOrNull { it.id == taskId } ?: return
        viewModelScope.launch {
            repository.updateTaskStatus(
                projectId = task.projectId,
                taskId = taskId,
                isDone = status == TaskStatus.DONE
            ) { success ->
                onComplete(success)
            }
        }
    }

    fun addUpdate(taskId: String, note: String) {
        // In a real app, this might be a comment or update in Firestore
        // For now, we can just log it or update a field if we had one
    }

    fun signOut() {
        auth.signOut()
        isLoaded = false
        _profile.value = null
    }
}
