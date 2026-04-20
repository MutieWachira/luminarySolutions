package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProjectDetailsUiState(
    val project: Project? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProjectDetailsViewModel : ViewModel() {
    private val repository = ProjectsRepository()
    private val _projectId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProjectDetailsUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf(ProjectDetailsUiState(isLoading = false))
            } else {
                repository.getProjectById(id).map { project ->
                    if (project != null) {
                        ProjectDetailsUiState(project = project, isLoading = false)
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
}
