package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.models.Project
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

data class LumiSphereUiState(
    val stats: LumiSphereOverviewDashboardStats = LumiSphereOverviewDashboardStats(),
    val programs: List<Project> = emptyList(),
    val donors: List<Donor> = emptyList(),
    val partners: List<Partner> = emptyList(),
    val approvals: List<Approval> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val searchQuery: String = "",
    val statusFilter: String = "All Status"
)

/**
 * LumiSphereDashboardViewModel: Specialized ViewModel for NGO Operations.
 * Handles impact metrics, program management, donors, and organizational approvals.
 */
@HiltViewModel
class LumiSphereDashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All Status")
    private val _isSaving = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LumiSphereUiState> = combine(
        _selectedYear.flatMapLatest { repository.getLumiSphereDashStats(it) },
        repository.getProjects(),
        repository.getDonors(),
        repository.getPartners(),
        repository.getApprovals(),
        _searchQuery,
        _statusFilter,
        _isSaving
    ) { args ->
        val stats = args[0] as LumiSphereOverviewDashboardStats
        val rawPrograms = args[1] as List<Project>
        val donors = args[2] as List<Donor>
        val partners = args[3] as List<Partner>
        val approvals = args[4] as List<Approval>
        val search = args[5] as String
        val status = args[6] as String
        val saving = args[7] as Boolean

        val filteredPrograms = rawPrograms.filter { p ->
            (p.name.contains(search, true) || p.description.contains(search, true)) &&
            (status == "All Status" || p.status == status)
        }

        LumiSphereUiState(
            stats = stats,
            programs = filteredPrograms,
            donors = donors,
            partners = partners,
            approvals = approvals,
            isLoading = false,
            isSaving = saving,
            searchQuery = search,
            statusFilter = status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LumiSphereUiState())

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateStatusFilter(filter: String) { _statusFilter.value = filter }

    fun addProgram(project: Project, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val finalProject = project.imageUrl?.takeIf { it.startsWith("content://") }?.let {
                project.copy(imageUrl = repository.uploadImage(Uri.parse(it)))
            } ?: project
            repository.addProject(finalProject).onSuccess {
                _isSaving.value = false
                onComplete(true)
            }.onFailure {
                _isSaving.value = false
                onComplete(false)
            }
        }
    }

    fun deleteProgram(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteProject(id).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun addDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addDonor(donor).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }

    fun addPartner(partner: Partner, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addPartner(partner).onSuccess { onComplete(true) }.onFailure { onComplete(false) }
        }
    }
}
