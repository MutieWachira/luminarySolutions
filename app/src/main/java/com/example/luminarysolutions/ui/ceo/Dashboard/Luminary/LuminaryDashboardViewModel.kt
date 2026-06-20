package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats
import com.example.luminarysolutions.data.models.*
import com.example.luminarysolutions.data.repository.DashboardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class LuminaryUiState(
    val stats: lumOverviewDashboardStats = lumOverviewDashboardStats(),
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
class LuminaryDashboardViewModel : ViewModel() {
    private val repository = DashboardRepository()

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
        val stats = args[0] as lumOverviewDashboardStats
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
            repository.addLuminaryProject(finalProject) {
                _isSaving.value = false
                if (it) {
                    _message.value = "Project added successfully."
                    _isError.value = false
                } else {
                    _message.value = "Failed to add project."
                    _isError.value = true
                }
                onComplete(it)
            }
        }
    }

    fun deleteProject(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteLuminaryProject(id) {
            if (it) {
                _message.value = "Project deleted."
                _isError.value = false
            } else {
                _message.value = "Failed to delete project."
                _isError.value = true
            }
            onComplete(it)
        }
    }

    fun addTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val finalTeam = team.imageUrl?.takeIf { it.startsWith("content://") }?.let {
                team.copy(imageUrl = repository.uploadImage(Uri.parse(it)))
            } ?: team
            val result = repository.addTeamMember(finalTeam)
            _isSaving.value = false
            if (result.isSuccess) {
                _message.value = "Team member added. Credentials sent to ${team.email}"
                _isError.value = false
                onComplete(true)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: ""
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
            val result = repository.deleteTeamMember(id)
            _isSaving.value = false
            if (result.isSuccess) {
                _message.value = "Team member and account removed permanently."
                _isError.value = false
                onComplete(true)
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "Failed to delete member"
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun addDocument(doc: Document, uri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val fileUrl = uri?.let { repository.uploadFile(it, doc.name) }
            repository.addDocument(doc.copy(fileUrl = fileUrl), onComplete)
        }
    }
}
