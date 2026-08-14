package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VolunteerDetailsUiState(
    val volunteer: Volunteer? = null,
    val volunteeredCampaigns: List<Project> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class VolunteerDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DashboardRepository
) : ViewModel() {

    private val volunteerId: String = checkNotNull(savedStateHandle["volunteerId"])

    private val _uiState = MutableStateFlow(VolunteerDetailsUiState())
    val uiState: StateFlow<VolunteerDetailsUiState> = _uiState.asStateFlow()

    init {
        loadVolunteerData()
    }

    private fun loadVolunteerData() {
        viewModelScope.launch {
            combine(
                repository.getVolunteerProfileFlow(volunteerId),
                repository.getAssignedProjects(listOf(volunteerId)),
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
            repository.updateVolunteerStatus(volunteerId, status)
        }
    }
}
