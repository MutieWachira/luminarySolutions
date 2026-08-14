package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.LumOverviewDashboardStats
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.TeamCulture
import com.example.luminarysolutions.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class LuminaryUiState(
    val stats: LumOverviewDashboardStats = LumOverviewDashboardStats(),
    val projects: List<Freelance> = emptyList(),
    val teams: List<Team> = emptyList(),
    val culture: TeamCulture = TeamCulture(),
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val searchQuery: String = "",
    val statusFilter: String = "All Status",
    val sortOrder: String = "Newest",
    val message: String? = null,
    val isError: Boolean = false
)

/**
 * LuminaryDashboardViewModel: Specialized ViewModel for Business Operations.
 */
@HiltViewModel
class LuminaryDashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All Status")
    private val _sortOrder = MutableStateFlow("Newest")
    private val _isSaving = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _isError = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LuminaryUiState> = combine(
        _selectedYear.flatMapLatest { repository.getLumDashStats(it) },
        repository.getLuminaryProjects(),
        repository.getTeams(),
        repository.getTeamCulture(),
        repository.getDocuments(),
        _searchQuery,
        _statusFilter,
        _sortOrder,
        _isSaving,
        _message,
        _isError
    ) { args ->
        val stats = args[0] as LumOverviewDashboardStats
        val rawProjects = args[1] as List<Freelance>
        val teams = args[2] as List<Team>
        val culture = args[3] as TeamCulture
        val docs = args[4] as List<Document>
        val search = args[5] as String
        val status = args[6] as String
        val sort = args[7] as String
        val saving = args[8] as Boolean
        val message = args[9] as? String
        val isError = args[10] as Boolean
        
        val filteredProjects = rawProjects.filter { p ->
            (p.name.contains(search, true) || p.description.contains(search, true)) &&
            (status == "All Status" || p.status == status)
        }.let { list ->
            when (sort) {
                "Newest" -> list.sortedByDescending { it.createdAt }
                "Oldest" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

        LuminaryUiState(
            stats = stats,
            projects = filteredProjects,
            teams = teams,
            culture = culture,
            documents = docs,
            isLoading = false,
            isSaving = saving,
            searchQuery = search,
            statusFilter = status,
            sortOrder = sort,
            message = message,
            isError = isError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LuminaryUiState())

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateStatusFilter(filter: String) { _statusFilter.value = filter }
    fun updateSortOrder(order: String) { _sortOrder.value = order }
    fun clearMessage() { _message.value = null; _isError.value = false }

    fun addProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val finalProject = freelance.imageUrl?.takeIf { it.startsWith("content://") }?.let {
                freelance.copy(imageUrl = repository.uploadImage(Uri.parse(it)))
            } ?: freelance
            repository.addLuminaryProject(finalProject).onSuccess {
                _isSaving.value = false
                _message.value = "Project added successfully."
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to add project."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun deleteProject(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteLuminaryProject(id).onSuccess {
                _message.value = "Project deleted."
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _message.value = "Failed to delete project."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun addTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val finalTeam = team.imageUrl?.takeIf { it.startsWith("content://") }?.let {
                team.copy(imageUrl = repository.uploadImage(Uri.parse(it)))
            } ?: team
            repository.addTeamMember(finalTeam).onSuccess {
                _isSaving.value = false
                _message.value = "Team member added. Credentials sent to ${team.email}"
                _isError.value = false
                onComplete(true)
            }.onFailure { e ->
                _isSaving.value = false
                val errorMsg = e.message ?: ""
                _message.value = when {
                    errorMsg.contains("password", true) -> "Error: Name is too short for auto-password. Use a longer full name."
                    errorMsg.contains("already exists", true) -> "Error: This email is already registered."
                    else -> "Backend failure occurred. Please try again."
                }
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun deleteTeamMember(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.deleteTeamMember(id).onSuccess {
                _isSaving.value = false
                _message.value = "Team member and account removed permanently."
                _isError.value = false
                onComplete(true)
            }.onFailure { e ->
                _isSaving.value = false
                _message.value = e.message ?: "Failed to delete member"
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun addDocument(doc: Document, uri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val fileUrl = uri?.let { repository.uploadFile(it, doc.name) }
            repository.addDocument(doc.copy(fileUrl = fileUrl)).onSuccess {
                onComplete(true)
            }.onFailure {
                onComplete(false)
            }
        }
    }
}
