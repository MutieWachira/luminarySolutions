package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ProjectFilter { ALL, ONGOING, COMPLETED, AT_RISK }

data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val teamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val filter: ProjectFilter = ProjectFilter.ALL,
    val isSaving: Boolean = false
)

class ProjectsViewModel : ViewModel() {
    private val repository = ProjectsRepository()

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(ProjectFilter.ALL)
    private val _isSaving = MutableStateFlow(false)

    val uiState: StateFlow<ProjectsUiState> = combine(
        repository.getProjects(),
        repository.getTeamMembers(),
        _searchQuery,
        _filter,
        _isSaving
    ) { projects, teamMembers, query, filter, isSaving ->
        val processedProjects = projects.map { project ->
            project.copy(status = computeStatus(project))
        }
        
        val filtered = processedProjects.filter { project ->
            val matchesFilter = when (filter) {
                ProjectFilter.ALL -> true
                ProjectFilter.ONGOING -> project.status == "Ongoing"
                ProjectFilter.COMPLETED -> project.status == "Completed"
                ProjectFilter.AT_RISK -> project.status == "At Risk"
            }
            val matchesQuery = project.name.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
        
        ProjectsUiState(
            projects = filtered,
            teamMembers = teamMembers,
            isLoading = false,
            searchQuery = query,
            filter = filter,
            isSaving = isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectsUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: ProjectFilter) {
        _filter.value = filter
    }

    fun addProject(project: Project, imageUri: Uri? = null) {
        Log.d("ProjectsViewModel", "Attempting to add project: ${project.name}")
        _isSaving.value = true
        
        viewModelScope.launch {
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) }
            
            // Clean up: ensure no local URI string persists
            val sanitizedImageUrl = if (finalImageUrl == null && project.imageUrl?.startsWith("content://") == true) {
                null
            } else {
                finalImageUrl ?: project.imageUrl
            }

            val projectWithImage = project.copy(
                id = UUID.randomUUID().toString(),
                imageUrl = sanitizedImageUrl,
                lastUpdated = "Just now"
            )
            
            repository.addProject(projectWithImage) { success ->
                _isSaving.value = false
                if (success) {
                    Log.d("ProjectsViewModel", "Project added successfully. URL: $sanitizedImageUrl")
                } else {
                    Log.e("ProjectsViewModel", "Failed to add project to Firestore")
                }
            }
        }
    }

    fun updateProject(project: Project, imageUri: Uri? = null) {
        Log.d("ProjectsViewModel", "Attempting to update project: ${project.name}")
        _isSaving.value = true

        viewModelScope.launch {
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) }
            
            val sanitizedImageUrl = if (finalImageUrl == null && project.imageUrl?.startsWith("content://") == true) {
                null
            } else {
                finalImageUrl ?: project.imageUrl
            }

            val updatedProject = project.copy(imageUrl = sanitizedImageUrl, lastUpdated = "Just now")

            repository.updateProject(updatedProject) { success ->
                _isSaving.value = false
                if (success) {
                    Log.d("ProjectsViewModel", "Project updated successfully. URL: $sanitizedImageUrl")
                } else {
                    Log.e("ProjectsViewModel", "Failed to update project in Firestore")
                }
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId) { success ->
                if (success) {
                    Log.d("ProjectsViewModel", "Project deleted successfully")
                } else {
                    Log.e("ProjectsViewModel", "Failed to delete project")
                }
            }
        }
    }

    private fun computeStatus(p: Project): String {
        val progressPercent = (p.progress * 100).toInt()
        if (progressPercent >= 100) return "Completed"

        val isNew = p.lastUpdated.equals("Today", true) || p.lastUpdated.contains("Just now", true) || p.lastUpdated.contains(":", true)
        if (isNew) return "Ongoing"

        val spentPercent =
            if (p.budget == 0) 0f else (p.spent.toFloat() / p.budget.toFloat()).coerceIn(0f, 1f)

        val budgetBurnTooHigh = spentPercent > (p.progress + 0.25f)
        val behindSchedule = p.progress < 0.5f
        val staleUpdate = p.lastUpdated.contains("week", ignoreCase = true)

        return if (budgetBurnTooHigh || behindSchedule || staleUpdate) "At Risk" else "Ongoing"
    }
}
