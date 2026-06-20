package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VolunteerDetailsUiState(
    val volunteer: Volunteer? = null,
    val volunteeredCampaigns: List<Project> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class VolunteerDetailsViewModel(
    private val volunteerId: String,
    private val repository: VolunteerRepository = VolunteerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VolunteerDetailsUiState())
    val uiState: StateFlow<VolunteerDetailsUiState> = _uiState.asStateFlow()

    init {
        loadVolunteerData()
    }

    private fun loadVolunteerData() {
        viewModelScope.launch {
            combine(
                repository.getVolunteerProfile(volunteerId),
                repository.getAssignedProjects(volunteerId),
                repository.getUnlockedAchievements(volunteerId)
            ) { volunteer, campaigns, achievements ->
                VolunteerDetailsUiState(
                    volunteer = volunteer,
                    volunteeredCampaigns = campaigns,
                    unlockedAchievements = achievements,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            FirestoreService.updateVolunteerStatus(volunteerId, status) { success ->
                if (success) {
                    // Data will be updated via flow
                }
            }
        }
    }
}
