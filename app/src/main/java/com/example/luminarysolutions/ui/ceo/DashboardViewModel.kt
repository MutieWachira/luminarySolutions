package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats
import com.example.luminarysolutions.data.models.Freelance
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
    val luminaryProjects: List<Freelance> = emptyList(),
    val teamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    val teams: List<com.example.luminarysolutions.data.models.Team> = emptyList(),
    val culture: com.example.luminarysolutions.data.models.TeamCulture = com.example.luminarysolutions.data.models.TeamCulture(),
    val approvals: List<Approval> = emptyList(),
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val isAddingProject: Boolean = false,
    val searchQuery: String = "",
    val statusFilter: String = "All Status",
    val sortOrder: String = "Newest",
    val teamSearchQuery: String = "",
    val teamStatusFilter: String = "All Status",
    val teamSortOrder: String = "Newest",
    val teamCurrentPage: Int = 1,
    val teamTotalPages: Int = 1,
    val totalTeamsCount: Int = 0,
    val totalActiveTeamsCount: Int = 0,
    val totalDepartmentsCount: Int = 0
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

    // Search and Filter states
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All Status")
    private val _sortOrder = MutableStateFlow("Newest")

    // Team Search and Filter states
    private val _teamSearchQuery = MutableStateFlow("")
    private val _teamStatusFilter = MutableStateFlow("All Status")
    private val _teamSortOrder = MutableStateFlow("Newest")
    private val _teamPage = MutableStateFlow(1)

    private val teamItemsPerPage = 6

    /**
     * Updates the selected year and triggers a fresh fetch of real-time monthly data.
     */
    fun updateSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun updateSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun updateTeamSearchQuery(query: String) {
        _teamSearchQuery.value = query
        _teamPage.value = 1 // Reset to first page on search
    }

    fun updateTeamStatusFilter(filter: String) {
        _teamStatusFilter.value = filter
        _teamPage.value = 1 // Reset to first page on filter change
    }

    fun updateTeamSortOrder(order: String) {
        _teamSortOrder.value = order
        _teamPage.value = 1 // Reset to first page on sort change
    }

    fun updateTeamPage(page: Int) {
        _teamPage.value = page
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CEODashboardUiState> = combine(
        _selectedYear,
        _searchQuery,
        _statusFilter,
        _sortOrder,
        _teamSearchQuery,
        _teamStatusFilter,
        _teamSortOrder,
        _teamPage
    ) { args ->
        val year = args[0] as Int
        val search = args[1] as String
        val status = args[2] as String
        val sort = args[3] as String
        val teamSearch = args[4] as String
        val teamStatus = args[5] as String
        val teamSort = args[6] as String
        val teamPage = args[7] as Int
        
        combine(
            repository.getDashboardStats(),
            repository.getLumDashStats(year),
            repository.getOngoingInitiatives(),
            repository.getLuminaryProjects(),
            repository.getRecentApprovals(),
            repository.getRecentDocuments(),
            repository.getTeamMembers(),
            repository.getTeams(),
            repository.getTeamCulture()
        ) { results: Array<Any> ->
            val rawFreelances = results[3] as List<Freelance>
            val rawTeams = results[7] as List<com.example.luminarysolutions.data.models.Team>
            val culture = results[8] as com.example.luminarysolutions.data.models.TeamCulture
            
            // Apply filtering and sorting to luminaryProjects
            val filteredFreelances = rawFreelances.filter { freelance ->
                val matchesSearch = freelance.name.contains(search, ignoreCase = true) ||
                                    freelance.description.contains(search, ignoreCase = true) ||
                                    freelance.category.contains(search, ignoreCase = true)
                val matchesStatus = status == "All Status" || freelance.status == status
                matchesSearch && matchesStatus
            }.let { list ->
                when (sort) {
                    "Newest" -> list.sortedByDescending { it.createdAt }
                    "Oldest" -> list.sortedBy { it.createdAt }
                    "Team Size" -> list.sortedByDescending { it.teamIds.size }
                    "Applicants" -> list.sortedByDescending { it.clientIds.size }
                    else -> list
                }
            }

            // Apply filtering and sorting to teams
            val filteredTeams = rawTeams.filter { team ->
                val matchesSearch = team.name.contains(teamSearch, ignoreCase = true) ||
                                    team.email.contains(teamSearch, ignoreCase = true) ||
                                    team.department.contains(teamSearch, ignoreCase = true) ||
                                    team.jobtitle.contains(teamSearch, ignoreCase = true)
                val matchesStatus = teamStatus == "All Status" || 
                                    (teamStatus == "Active" && team.enabled) || 
                                    (teamStatus == "Inactive" && !team.enabled)
                matchesSearch && matchesStatus
            }.let { list ->
                when (teamSort) {
                    "Newest" -> list // Firestore doesn't have createdAt yet, but we could add it
                    "Name (A-Z)" -> list.sortedBy { it.name }
                    "Name (Z-A)" -> list.sortedByDescending { it.name }
                    "Department" -> list.sortedBy { it.department }
                    else -> list
                }
            }

            // Paginate teams
            val totalTeams = filteredTeams.size
            val activeTeams = filteredTeams.count { it.enabled }
            val depts = filteredTeams.map { it.department }.distinct().size
            
            val totalPages = if (totalTeams == 0) 1 else kotlin.math.ceil(totalTeams.toDouble() / teamItemsPerPage).toInt()
            val safePage = teamPage.coerceIn(1, totalPages)
            val paginatedTeams = filteredTeams.drop((safePage - 1) * teamItemsPerPage).take(teamItemsPerPage)

            CEODashboardUiState(
                stats = results[0] as DashboardStats,
                lumStats = results[1] as lumOverviewDashboardStats,
                initiatives = results[2] as List<Project>,
                luminaryProjects = filteredFreelances,
                approvals = results[4] as List<Approval>,
                documents = results[5] as List<Document>,
                teamMembers = results[6] as List<com.example.luminarysolutions.data.models.User>,
                teams = paginatedTeams,
                culture = culture,
                isLoading = false,
                searchQuery = search,
                statusFilter = status,
                sortOrder = sort,
                teamSearchQuery = teamSearch,
                teamStatusFilter = teamStatus,
                teamSortOrder = teamSort,
                teamCurrentPage = safePage,
                teamTotalPages = totalPages,
                totalTeamsCount = totalTeams,
                totalActiveTeamsCount = activeTeams,
                totalDepartmentsCount = depts
            )
        }
    }.flatMapLatest { it }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CEODashboardUiState()
    )

    fun addLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalFreelance = if (freelance.imageUrl != null && freelance.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(freelance.imageUrl))
                freelance.copy(imageUrl = uploadedUrl)
            } else {
                freelance
            }
            repository.addLuminaryProject(finalFreelance, onComplete)
        }
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        repository.deleteLuminaryProject(projectId, onComplete)
    }

    fun updateLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalFreelance = if (freelance.imageUrl != null && freelance.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(freelance.imageUrl))
                freelance.copy(imageUrl = uploadedUrl)
            } else {
                freelance
            }
            repository.updateLuminaryProject(finalFreelance, onComplete)
        }
    }

    // Team Management
    fun addTeamMember(team: com.example.luminarysolutions.data.models.Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalTeam = if (team.imageUrl != null && team.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(team.imageUrl))
                team.copy(imageUrl = uploadedUrl)
            } else {
                team
            }
            repository.addTeamMember(finalTeam, onComplete)
        }
    }

    fun updateTeamMember(team: com.example.luminarysolutions.data.models.Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val finalTeam = if (team.imageUrl != null && team.imageUrl.startsWith("content://")) {
                val uploadedUrl = repository.uploadImage(Uri.parse(team.imageUrl))
                team.copy(imageUrl = uploadedUrl)
            } else {
                team
            }
            repository.updateTeamMember(finalTeam, onComplete)
        }
    }

    fun deleteTeamMember(teamId: String, onComplete: (Boolean) -> Unit) {
        repository.deleteTeamMember(teamId, onComplete)
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

