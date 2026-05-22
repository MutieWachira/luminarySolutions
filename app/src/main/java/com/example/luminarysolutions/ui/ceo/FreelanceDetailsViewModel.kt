package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.DashboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FreelanceDetailsUiState(
    val freelance: Freelance? = null,
    val assignedTeam: List<User> = emptyList(),
    val applicants: List<User> = emptyList(),
    val allTeamMembers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FreelanceDetailsViewModel : ViewModel() {
    private val repository = DashboardRepository()
    private val _projectId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FreelanceDetailsUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(FreelanceDetailsUiState(isLoading = false))
            } else {
                repository.getLuminaryProjectById(id).flatMapLatest { freelance ->
                    if (freelance != null) {
                        combine(
                            repository.getUsersByIds(freelance.teamIds),
                            repository.getUsersByIds(freelance.clientIds),
                            repository.getTeamMembers()
                        ) { assigned, applicants, allMembers ->
                            FreelanceDetailsUiState(
                                freelance = freelance,
                                assignedTeam = assigned,
                                applicants = applicants,
                                allTeamMembers = allMembers,
                                isLoading = false
                            )
                        }
                    } else {
                        flowOf(FreelanceDetailsUiState(isLoading = false, error = "Service not found"))
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FreelanceDetailsUiState()
        )

    fun loadProject(projectId: String) {
        _projectId.value = projectId
    }

    fun updateFreelance(freelance: Freelance) {
        repository.updateLuminaryProject(freelance) { success ->
            // Handle result
        }
    }

    fun assignToTeam(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (!currentFreelance.teamIds.contains(userId)) {
            val updatedTeam = currentFreelance.teamIds + userId
            // Also remove from applicants if they were there
            val updatedApplicants = currentFreelance.clientIds - userId
            updateFreelance(currentFreelance.copy(teamIds = updatedTeam, clientIds = updatedApplicants))
        }
    }

    fun removeFromTeam(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (currentFreelance.teamIds.contains(userId)) {
            val updatedTeam = currentFreelance.teamIds - userId
            updateFreelance(currentFreelance.copy(teamIds = updatedTeam))
        }
    }
    
    fun rejectApplicant(userId: String) {
        val currentFreelance = uiState.value.freelance ?: return
        if (currentFreelance.clientIds.contains(userId)) {
            val updatedApplicants = currentFreelance.clientIds - userId
            updateFreelance(currentFreelance.copy(clientIds = updatedApplicants))
        }
    }
}
