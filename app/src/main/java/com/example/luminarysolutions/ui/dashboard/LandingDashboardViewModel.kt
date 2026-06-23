package com.example.luminarysolutions.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.data.repository.AuthStatus
import com.example.luminarysolutions.ui.donor.data.DonorRepository
import com.example.luminarysolutions.ui.donor.data.DonorRepositoryImpl
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State representing the Landing Dashboard UI.
 */
data class LandingDashboardUiState(
    val totalRaised: Int = 0,
    val activeCampaigns: Int = 0,
    val impactReached: String = "15K+",
    val featuredCampaigns: List<CampaignUi> = emptyList(),
    val categories: List<String> = listOf("All", "Education", "Health", "Environment", "Water", "Community", "Entertainment"),
    val selectedCategory: String = "All",
    val heroImages: List<String> = listOf(
        "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=1000",
        "https://images.unsplash.com/photo-1532629345422-7515f3d16bb8?q=80&w=1000",
        "https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?q=80&w=1000"
    ),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Landing Dashboard.
 * Handles public data fetching and business logic for the entry screen.
 */
class LandingDashboardViewModel(
    private val donorRepository: DonorRepository = DonorRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingDashboardUiState(isLoading = true))
    val uiState: StateFlow<LandingDashboardUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    private var statsJob: Job? = null
    private var dashboardJob: Job? = null

    init {
        observeAuthStatus()
        observeGlobalStats()
        observeCampaigns()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            authRepository.getAuthState().collect { status ->
                _uiState.update { it.copy(isLoggedIn = status is AuthStatus.Authenticated) }
            }
        }
    }

    /**
     * Updates the selected category and re-fetches campaigns.
     */
    fun onCategorySelected(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
        observeCampaigns()
    }

    /**
     * Observes all campaigns to calculate global impact metrics.
     */
    private fun observeGlobalStats() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            donorRepository.getCampaignsFlow(null)
                .catch { e ->
                    _uiState.update { it.copy(error = "Failed to fetch stats: ${e.localizedMessage}") }
                }
                .collect { allCampaigns ->
                    val totalRaised = allCampaigns.sumOf { it.raisedAmount }
                    // Simulated calculation: $1 raised helps approx 1 person, plus base impact per campaign
                    val totalImpactValue = (allCampaigns.size * 1250) + (totalRaised / 100) 
                    _uiState.update { it.copy(
                        totalRaised = totalRaised,
                        impactReached = "${totalImpactValue / 1000}K+"
                    ) }
                }
        }
    }

    /**
     * Observes campaigns in real-time, optionally filtered by category.
     */
    private fun observeCampaigns() {
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            val category = if (_selectedCategory.value == "All") null else _selectedCategory.value
            
            donorRepository.getCampaignsFlow(category)
                .catch { e ->
                    _uiState.update { it.copy(
                        error = "Unable to load campaigns. ${e.localizedMessage}",
                        isLoading = false
                    ) }
                }
                .collect { campaigns ->
                    _uiState.update {
                        it.copy(
                            activeCampaigns = campaigns.size,
                            featuredCampaigns = campaigns,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}
