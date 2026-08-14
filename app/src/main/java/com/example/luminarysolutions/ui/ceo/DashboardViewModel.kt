package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.LumOverviewDashboardStats
import com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Event
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.TeamCulture
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CEODashboardUiState(
    val userName: String = "Executive",
    val generalStats: DashboardStats = DashboardStats(),
    val lumStats: LumOverviewDashboardStats = LumOverviewDashboardStats(),
    val lumiSphereStats: LumiSphereOverviewDashboardStats = LumiSphereOverviewDashboardStats(),
    val recentInitiatives: List<Project> = emptyList(),
    val recentApprovals: List<Approval> = emptyList(),
    val recentDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val luminaryProjects: List<Freelance> = emptyList(),
    val teams: List<Team> = emptyList(),
    val culture: TeamCulture = TeamCulture(),
    val documents: List<Document> = emptyList(),
    val initiatives: List<Project> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "All Status",
    val sortOrder: String = "Newest",
    val teamSearchQuery: String = "",
    val teamStatusFilter: String = "All Status",
    val teamSortOrder: String = "Newest",
    val teamCurrentPage: Int = 1,
    val teamTotalPages: Int = 1,
    val docSearchQuery: String = "",
    val docCategoryFilter: String = "All Categories",
    val docSortOrder: String = "Newest",
    val docCurrentPage: Int = 1,
    val docTotalPages: Int = 1,
    val docStats: Map<String, Int> = emptyMap(),
    val totalDocsCount: Int = 0,
    val totalTeamsCount: Int = 0,
    val totalActiveTeamsCount: Int = 0,
    val totalDepartmentsCount: Int = 0,
    val programSearchQuery: String = "",
    val programStatusFilter: String = "All Status",
    val programSortOrder: String = "Newest",
    val programCurrentPage: Int = 1,
    val programTotalPages: Int = 1,
    val volunteers: List<Volunteer> = emptyList(),
    val volunteerApplications: List<Volunteer> = emptyList(),
    val volunteerSearchQuery: String = "",
    val events: List<Event> = emptyList(),
    val eventSearchQuery: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val totalProgramsCount: Int = 0,
    val fundingTrend: String = "+0%",
    val isFundingPositive: Boolean = true,
    val burnTrend: String = "+0%",
    val isBurnPositive: Boolean = false,
    val programsTrend: String = "+0%",
    val isProgramsPositive: Boolean = true,
    val reachTrend: String = "+0%",
    val isReachPositive: Boolean = true
)

@HiltViewModel
class CEODashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All Status")
    private val _sortOrder = MutableStateFlow("Newest")
    
    private val _teamSearchQuery = MutableStateFlow("")
    private val _teamStatusFilter = MutableStateFlow("All Status")
    private val _teamSortOrder = MutableStateFlow("Newest")
    private val _teamPage = MutableStateFlow(1)
    
    private val _docSearchQuery = MutableStateFlow("")
    private val _docCategoryFilter = MutableStateFlow("All Categories")
    private val _docSortOrder = MutableStateFlow("Newest")
    private val _docPage = MutableStateFlow(1)

    private val _programSearchQuery = MutableStateFlow("")
    private val _programStatusFilter = MutableStateFlow("All Status")
    private val _programSortOrder = MutableStateFlow("Newest")
    private val _programPage = MutableStateFlow(1)
    private val _volunteerSearchQuery = MutableStateFlow("")
    private val _eventSearchQuery = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)

    private val _message = MutableStateFlow<String?>(null)
    private val _isError = MutableStateFlow(false)

    private val _userName = MutableStateFlow("Executive")

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                repository.getUserProfile(uid).collect { user ->
                    user?.let { _userName.value = it.name }
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CEODashboardUiState> = combine(
        repository.getDashboardStats(),
        _selectedYear.flatMapLatest { repository.getLumDashStats(it) },
        _selectedYear.flatMapLatest { repository.getLumiSphereDashStats(it) },
        repository.getProjects(), 
        repository.getRecentApprovals(),
        repository.getDocuments(),
        repository.getLuminaryProjects(),
        repository.getTeams(),
        repository.getTeamCulture(),
        repository.getVolunteers(),
        repository.getVolunteerApplications(),
        repository.getEvents(),
        _searchQuery,
        _statusFilter,
        _sortOrder,
        _teamSearchQuery,
        _teamStatusFilter,
        _teamSortOrder,
        _teamPage,
        _docSearchQuery,
        _docCategoryFilter,
        _docSortOrder,
        _docPage,
        _programSearchQuery,
        _programStatusFilter,
        _programSortOrder,
        _programPage,
        _volunteerSearchQuery,
        _eventSearchQuery,
        _isSaving,
        _message,
        _isError,
        _userName
    ) { args ->
        val generalStats = args[0] as DashboardStats
        val lumStats = args[1] as LumOverviewDashboardStats
        val lumiSphereStats = args[2] as LumiSphereOverviewDashboardStats
        val allLumiSphereProjects = args[3] as List<Project>
        val recentApprovals = args[4] as List<Approval>
        val allDocs = args[5] as List<Document>
        val luminaryProjects = args[6] as List<Freelance>
        val allTeams = args[7] as List<Team>
        val culture = args[8] as TeamCulture
        val volunteers = args[9] as List<Volunteer>
        val volunteerApps = args[10] as List<Volunteer>
        val allEvents = args[11] as List<Event>
        
        val search = args[12] as String
        val status = args[13] as String
        val sort = args[14] as String
        
        val teamSearch = args[15] as String
        val teamStatus = args[16] as String
        val teamSort = args[17] as String
        val teamPage = args[18] as Int
        
        val docSearch = args[19] as String
        val docCategory = args[20] as String
        val docSort = args[21] as String
        val docPage = args[22] as Int

        val progSearch = args[23] as String
        val progStatus = args[24] as String
        val progSort = args[25] as String
        val progPage = args[26] as Int
        val volunteerSearch = args[27] as String
        val eventSearch = args[28] as String
        val saving = args[29] as Boolean
        val message = args[30] as? String
        val isError = args[31] as Boolean
        val userName = args[32] as String

        // Quarterly Trend Calculation Logic
        val monthOrder = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentQuarter = currentMonth / 3
        val previousQuarter = if (currentQuarter == 0) 3 else currentQuarter - 1

        fun calculateTrend(currentSum: Int, previousSum: Int): String {
            if (previousSum == 0) return "+0%"
            val diff = ((currentSum - previousSum).toFloat() / previousSum.toFloat()) * 100
            val prefix = if (diff >= 0) "+" else ""
            return "$prefix${diff.toInt()}%"
        }

        // Trends for Funding (Donations)
        val currentQuarterDonations = lumiSphereStats.monthlyStats
            .filter { monthOrder.indexOf(it.month.lowercase()) / 3 == currentQuarter }
            .sumOf { it.donations }
        val previousQuarterDonations = lumiSphereStats.monthlyStats
            .filter { monthOrder.indexOf(it.month.lowercase()) / 3 == previousQuarter }
            .sumOf { it.donations }
        val fundingTrend = calculateTrend(currentQuarterDonations, previousQuarterDonations)

        // Trends for Burn (Expenses from Luminary)
        val currentQuarterExpenses = lumStats.monthlyStats
            .filter { monthOrder.indexOf(it.month.lowercase()) / 3 == currentQuarter }
            .sumOf { it.expenses }
        val previousQuarterExpenses = lumStats.monthlyStats
            .filter { monthOrder.indexOf(it.month.lowercase()) / 3 == previousQuarter }
            .sumOf { it.expenses }
        val burnTrend = calculateTrend(currentQuarterExpenses, previousQuarterExpenses)

        // Filtering Luminary Projects (Freelance)
        val filteredLumProjects = luminaryProjects.filter { project ->
            val matchesSearch = project.name.contains(search, true) || project.description.contains(search, true) || project.category.contains(search, true)
            val matchesStatus = status == "All Status" || project.status == status
            matchesSearch && matchesStatus
        }.let { list ->
            when (sort) {
                "Newest" -> list.sortedByDescending { it.createdAt }
                "Oldest" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

        // Filtering Teams
        val filteredTeams = allTeams.filter { member ->
            val matchesSearch = member.name.contains(teamSearch, true) || member.email.contains(teamSearch, true) || member.department.contains(teamSearch, true)
            val matchesStatus = teamStatus == "All Status" || (if (teamStatus == "Active") member.enabled else !member.enabled)
            matchesSearch && matchesStatus
        }.let { list ->
            when (teamSort) {
                "Name (A-Z)" -> list.sortedBy { it.name }
                "Name (Z-A)" -> list.sortedByDescending { it.name }
                else -> list
            }
        }

        // Filtering Documents
        val filteredDocs = allDocs.filter { it.name.contains(docSearch, true) && (docCategory == "All Categories" || it.category == docCategory) }
        
        // Filtering LumiSphere Programs (Projects)
        val filteredPrograms = allLumiSphereProjects.filter { 
            it.name.contains(progSearch, true) || it.description.contains(progSearch, true) || it.location.contains(progSearch, true) || it.category.contains(progSearch, true)
        }.let { list ->
            if (progStatus == "All Status") list else list.filter { it.status == progStatus }
        }.let { list ->
            when (progSort) {
                "Newest" -> list.sortedByDescending { it.startDate }
                "Oldest" -> list.sortedBy { it.startDate }
                "Progress" -> list.sortedByDescending { it.progress }
                else -> list
            }
        }
        
        // Filtering Volunteers
        val filteredVolunteers = volunteers.filter { it.name.contains(volunteerSearch, true) || it.email.contains(volunteerSearch, true) }
        val filteredApps = volunteerApps.filter { it.name.contains(volunteerSearch, true) || it.email.contains(volunteerSearch, true) }

        // Filtering Events
        val filteredEvents = allEvents.filter { it.title.contains(eventSearch, true) || it.location.contains(eventSearch, true) }

        // BUSINESS RULE: Total Programs for CEO Dashboard = Luminary (Freelance) + LumiSphere (Project)
        // We use the stats from Firestore for consistency, or the list size if stats are 0
        val totalLumCount = if (lumStats.totalProjects > 0) lumStats.totalProjects else luminaryProjects.size
        val totalLumiSphereCount = if (lumiSphereStats.totalPrograms > 0) lumiSphereStats.totalPrograms else allLumiSphereProjects.size
        val totalProgramsCount = totalLumCount + totalLumiSphereCount

        CEODashboardUiState(
            userName = userName,
            generalStats = generalStats,
            lumStats = lumStats,
            // BUSINESS RULE: LumiSphere details should ONLY show LumiSphere specific counts
            lumiSphereStats = lumiSphereStats.copy(totalPrograms = allLumiSphereProjects.size), 
            recentInitiatives = allLumiSphereProjects.take(5),
            recentApprovals = recentApprovals,
            recentDocuments = allDocs.take(3),
            isLoading = false,
            luminaryProjects = filteredLumProjects,
            teams = filteredTeams,
            culture = culture,
            documents = filteredDocs,
            initiatives = filteredPrograms,
            searchQuery = search,
            statusFilter = status,
            sortOrder = sort,
            teamSearchQuery = teamSearch,
            teamStatusFilter = teamStatus,
            teamSortOrder = teamSort,
            teamCurrentPage = teamPage,
            teamTotalPages = (filteredTeams.size / 10).coerceAtLeast(1),
            docSearchQuery = docSearch,
            docCategoryFilter = docCategory,
            docSortOrder = docSort,
            docCurrentPage = docPage,
            docTotalPages = (filteredDocs.size / 10).coerceAtLeast(1),
            docStats = allDocs.groupBy { it.category }.mapValues { it.value.size },
            totalDocsCount = allDocs.size,
            totalTeamsCount = allTeams.size,
            totalActiveTeamsCount = allTeams.count { it.enabled },
            totalDepartmentsCount = allTeams.map { it.department }.distinct().size,
            programSearchQuery = progSearch,
            programStatusFilter = progStatus,
            programSortOrder = progSort,
            programCurrentPage = progPage,
            programTotalPages = (filteredPrograms.size / 10).coerceAtLeast(1),
            volunteers = filteredVolunteers,
            volunteerApplications = filteredApps,
            volunteerSearchQuery = volunteerSearch,
            events = filteredEvents,
            eventSearchQuery = eventSearch,
            isSaving = saving,
            message = message,
            isError = isError,
            totalProgramsCount = totalProgramsCount,
            fundingTrend = fundingTrend,
            isFundingPositive = fundingTrend.startsWith("+"),
            burnTrend = burnTrend,
            isBurnPositive = !burnTrend.startsWith("+"), // Lower burn is positive
            programsTrend = "+2.4%", // Mocked quarterly trend
            isProgramsPositive = true,
            reachTrend = "+18%", // Mocked quarterly trend
            isReachPositive = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CEODashboardUiState())


    fun updateYear(year: Int) { _selectedYear.value = year }
    fun updateSelectedYear(year: Int) { _selectedYear.value = year }
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateStatusFilter(filter: String) { _statusFilter.value = filter }
    fun updateSortOrder(order: String) { _sortOrder.value = order }
    fun updateTeamSearchQuery(query: String) { _teamSearchQuery.value = query }
    fun updateTeamStatusFilter(filter: String) { _teamStatusFilter.value = filter }
    fun updateTeamSortOrder(order: String) { _teamSortOrder.value = order }
    fun updateTeamPage(page: Int) { _teamPage.value = page }
    fun updateDocSearchQuery(query: String) { _docSearchQuery.value = query }
    fun updateDocCategoryFilter(filter: String) { _docCategoryFilter.value = filter }
    fun updateDocSortOrder(order: String) { _docSortOrder.value = order }
    fun updateDocPage(page: Int) { _docPage.value = page }
    fun updateProgramSearchQuery(query: String) { _programSearchQuery.value = query }
    fun updateProgramStatusFilter(filter: String) { _programStatusFilter.value = filter }
    fun updateProgramSortOrder(order: String) { _programSortOrder.value = order }
    fun updateProgramPage(page: Int) { _programPage.value = page }
    fun updateVolunteerSearchQuery(query: String) { _volunteerSearchQuery.value = query }
    fun updateEventSearchQuery(query: String) { _eventSearchQuery.value = query }

    fun addLuminaryProject(freelance: Freelance, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _message.value = "Uploading image and saving project..."
            
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) } 
            
            if (imageUri != null && finalImageUrl == null) {
                _message.value = "Image upload failed. Project saved without image."
                _isError.value = true
            }

            val sanitizedImageUrl = if (finalImageUrl == null && freelance.imageUrl?.startsWith("content://") == true) {
                null
            } else {
                finalImageUrl ?: freelance.imageUrl
            }

            repository.addLuminaryProject(freelance.copy(imageUrl = sanitizedImageUrl)).onSuccess {
                _isSaving.value = false
                _message.value = if (finalImageUrl == null && imageUri != null) 
                    "Project added (image failed)" else "Project added successfully"
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to add project to database."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun updateLuminaryProject(freelance: Freelance, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _message.value = "Updating project..."
            
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) }
            
            val sanitizedImageUrl = if (finalImageUrl == null && freelance.imageUrl?.startsWith("content://") == true) {
                null // Don't persist temporary URIs
            } else {
                finalImageUrl ?: freelance.imageUrl
            }

            repository.updateLuminaryProject(freelance.copy(imageUrl = sanitizedImageUrl)).onSuccess {
                _isSaving.value = false
                _message.value = "Project updated"
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Update failed"
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun deleteLuminaryProject(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteLuminaryProject(id).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }
    
    fun addTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.addTeamMember(team).onSuccess {
                _isSaving.value = false
                _message.value = "Team member added successfully. Email sent."
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to add team member."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
        _isError.value = false
    }

    fun deleteTeamMember(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.deleteTeamMember(id).onSuccess {
                _isSaving.value = false
                _message.value = "Team member removed."
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to remove team member."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun updateTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.updateTeamMember(team).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }
    
    fun addDocument(doc: Document, uri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val fileUrl = uri?.let { repository.uploadFile(it, doc.name) }
            repository.addDocument(doc.copy(fileUrl = fileUrl)).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }
    fun deleteDocument(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteDocument(id).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun addProgram(project: Project, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _message.value = "Uploading and creating program..."
            
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) }
            
            if (imageUri != null && finalImageUrl == null) {
                _message.value = "Image upload failed. Program created without image."
                _isError.value = true
            }

            val sanitizedImageUrl = if (finalImageUrl == null && project.imageUrl?.startsWith("content://") == true) {
                null
            } else {
                finalImageUrl ?: project.imageUrl
            }

            repository.addProject(project.copy(imageUrl = sanitizedImageUrl)).onSuccess {
                _isSaving.value = false
                _message.value = if (finalImageUrl == null && imageUri != null) 
                    "Program added (image failed)" else "Program added successfully"
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to create program."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun updateProgram(project: Project, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _message.value = "Updating program details..."
            
            val finalImageUrl = imageUri?.let { repository.uploadImage(it) }
            
            val sanitizedImageUrl = if (finalImageUrl == null && project.imageUrl?.startsWith("content://") == true) {
                null
            } else {
                finalImageUrl ?: project.imageUrl
            }

            repository.updateProject(project.copy(imageUrl = sanitizedImageUrl)).onSuccess {
                _isSaving.value = false
                _message.value = "Program updated successfully"
                _isError.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                _message.value = "Failed to update program."
                _isError.value = true
                onComplete(false)
            }
        }
    }

    fun deleteProgram(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteProject(id).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun updateVolunteerStatus(volunteerId: String, status: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.updateVolunteerStatus(volunteerId, status).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun updateVolunteer(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            repository.updateVolunteer(volunteer).onSuccess {
                _isSaving.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                onComplete(false)
            }
        }
    }

    fun deleteVolunteer(volunteerId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteVolunteer(volunteerId).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    // Event Management
    fun addEvent(event: Event, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addEvent(event).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun updateEvent(event: Event, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.updateEvent(event).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun deleteEvent(eventId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteEvent(eventId).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }
}
