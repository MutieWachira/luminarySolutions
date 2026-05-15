package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats
import com.example.luminarysolutions.data.repository.DashboardRepository
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Document
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CEODashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val lumStats: lumOverviewDashboardStats = lumOverviewDashboardStats(),
    val initiatives: List<Project> = emptyList(),
    val luminaryProjects: List<Project> = emptyList(),
    val teamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    val approvals: List<Approval> = emptyList(),
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isAddingProject: Boolean = false
)

data class LumOverviewDashboardUiState(
    val lumstats: lumOverviewDashboardStats = lumOverviewDashboardStats(),
    val isLoading: Boolean = true
)

class CEODashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    // Real-time year selection for financial data
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    /**
     * Updates the selected year and triggers a fresh fetch of real-time monthly data.
     */
    fun updateSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CEODashboardUiState> = _selectedYear.flatMapLatest { year ->
        combine(
            repository.getDashboardStats(),
            repository.getLumDashStats(year),
            repository.getOngoingInitiatives(),
            repository.getLuminaryProjects(),
            repository.getRecentApprovals(),
            repository.getRecentDocuments(),
            repository.getTeamMembers()
        ) { args: Array<Any> ->
            CEODashboardUiState(
                stats = args[0] as DashboardStats,
                lumStats = args[1] as lumOverviewDashboardStats,
                initiatives = args[2] as List<Project>,
                luminaryProjects = args[3] as List<Project>,
                approvals = args[4] as List<Approval>,
                documents = args[5] as List<Document>,
                teamMembers = args[6] as List<com.example.luminarysolutions.data.models.User>,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CEODashboardUiState()
    )

    fun addLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalProject = if (project.imageUrl != null && project.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(project.imageUrl))
                project.copy(imageUrl = uploadedUrl)
            } else {
                project
            }
            repository.addLuminaryProject(finalProject, onComplete)
        }
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        repository.deleteLuminaryProject(projectId, onComplete)
    }

    fun updateLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalProject = if (project.imageUrl != null && project.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(project.imageUrl))
                project.copy(imageUrl = uploadedUrl)
            } else {
                project
            }
            repository.updateLuminaryProject(finalProject, onComplete)
        }
    }
}

class LumOverviewDashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    val uiState: StateFlow<LumOverviewDashboardUiState> = repository.getLumDashStats(Calendar.getInstance().get(Calendar.YEAR)).map { lumstats ->
        LumOverviewDashboardUiState(
            lumstats = lumstats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LumOverviewDashboardUiState()
    )
}

