package com.example.luminarysolutions.ui.donor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.repository.FinanceRepository
import com.example.luminarysolutions.data.repository.GamificationRepository
import com.example.luminarysolutions.ui.donor.data.DonorRepository
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Donor Module.
 */
data class DonorUiState(
    val campaigns: List<CampaignUi> = emptyList(),
    val donations: List<DonationUi> = emptyList(),
    val reports: List<ImpactReportUi> = emptyList(),
    val summaryStats: com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats = com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "All",
    val uiMessage: String? = null,
    val isVolunteering: Boolean = false,
    val isVolunteeringForCurrentCampaign: Boolean = false,
    val isVolunteerRole: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
    val error: String? = null,
    val donor: Donor? = null,
    val allAchievements: List<Achievement> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList()
)

/**
 * Production-ready ViewModel for the Donor module.
 * Implements MVVM with Clean Architecture principles.
 */
@HiltViewModel
class DonorViewModel @Inject constructor(
    private val repo: DonorRepository,
    private val financeRepo: FinanceRepository,
    private val gamificationRepo: GamificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonorUiState())
    val uiState: StateFlow<DonorUiState> = _uiState.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")

    init {
        viewModelScope.launch { gamificationRepo.seedAchievements() }
        checkAuthStatus()
        observeCampaigns()
        observeReports()
        observeAchievements()
        observeSummaryStats()
    }

    private fun observeSummaryStats() {
        viewModelScope.launch {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            financeRepo.getLumiSphereDashStats(currentYear).collect { stats ->
                _uiState.update { it.copy(summaryStats = stats) }
            }
        }
    }

    private fun observeAchievements() {
        viewModelScope.launch {
            gamificationRepo.getAchievements().collect { all ->
                // Robust Filter: Check role field OR ID prefix as fallback for legacy data
                val donorAchievements = all.filter { 
                    it.role == "DONOR" || 
                    it.id.startsWith("donor_") || 
                    it.id.startsWith("amt_") || 
                    it.id.startsWith("div_") || 
                    it.id.startsWith("eng_") || 
                    it.id.startsWith("spec_")
                }
                _uiState.update { it.copy(allAchievements = donorAchievements) }
                updateUnlockedAchievements()
            }
        }
    }

    private fun updateUnlockedAchievements() {
        val donor = _uiState.value.donor ?: return
        val all = _uiState.value.allAchievements
        val unlocked = all.filter { donor.achievements.contains(it.id) }
        _uiState.update { it.copy(unlockedAchievements = unlocked) }
    }

    private fun checkAuthStatus() {
        val user = auth.currentUser
        _uiState.update { it.copy(
            isLoggedIn = user != null,
            currentUserId = user?.uid
        ) }
        
        user?.let {
            loadUserStats(it.uid)
        }
    }

    /**
     * Real-time observation of campaigns with category filtering.
     * Improved with error handling and state management.
     */
    private fun observeCampaigns() {
        viewModelScope.launch {
            _categoryFilter.flatMapLatest { category ->
                repo.getCampaignsFlow(if (category == "All") null else category)
                    .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                    .catch { e -> 
                        _uiState.update { it.copy(error = e.message ?: "Failed to load campaigns", isLoading = false) }
                    }
            }.collect { list ->
                _uiState.update { it.copy(campaigns = list, isLoading = false, error = null) }
            }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            _categoryFilter.flatMapLatest { category ->
                repo.getReports(if (category == "All") null else category)
                    .onStart { _uiState.update { it.copy(isLoading = true) } }
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
            }.collect { list ->
                _uiState.update { it.copy(reports = list, isLoading = false) }
            }
        }
    }

    private var userStatsJob: kotlinx.coroutines.Job? = null

    fun loadUserStats(userId: String) {
        userStatsJob?.cancel()
        userStatsJob = viewModelScope.launch {
            // Observe donor profile
            launch {
                financeRepo.getDonorById(userId).collect { donor ->
                    _uiState.update { it.copy(donor = donor) }
                    updateUnlockedAchievements()
                }
            }
            // Combine both observations into one job with error handling
            launch {
                repo.getMyDonations(userId)
                    .catch { e -> _uiState.update { it.copy(error = e.localizedMessage) } }
                    .collect { list ->
                        _uiState.update { it.copy(donations = list) }
                    }
            }
            launch {
                try {
                    val isVolunteering = repo.isAlreadyVolunteering(userId)
                    val isVolunteerRole = repo.isVolunteer(userId)
                    _uiState.update { it.copy(
                        isVolunteering = isVolunteering,
                        isVolunteerRole = isVolunteerRole
                    ) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.localizedMessage) }
                }
            }
        }
    }

    fun checkVolunteerStatusForCampaign(campaignId: String) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            val isForCampaign = repo.isVolunteeringForCampaign(userId, campaignId)
            _uiState.update { it.copy(isVolunteeringForCurrentCampaign = isForCampaign) }
        }
    }

    fun joinProject(campaignId: String) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.joinProject(userId, campaignId).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isVolunteering = true,
                        isVolunteeringForCurrentCampaign = true,
                        uiMessage = "Welcome to the team! You've successfully joined this project."
                    )
                }
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        uiMessage = e.message ?: "Failed to join project."
                    )
                }
            }
        }
    }

    fun setCategory(category: String) {
        _categoryFilter.value = category
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
    }

    fun getCampaign(campaignId: String): Flow<CampaignUi?> = 
        repo.getCampaign(campaignId)
            .catch { e -> 
                _uiState.update { it.copy(error = e.localizedMessage) }
                emit(null)
            }

    /**
     * Handles donation logic.
     */
    fun donate(userId: String, campaignId: String, amount: Int) {
        if (amount <= 0) {
            _uiState.update { it.copy(uiMessage = "Please enter a valid amount.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.donate(userId, campaignId, amount).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        uiMessage = "Donation successful! Thank you for your support."
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        uiMessage = "Donation failed. Please try again."
                    )
                }
            }
        }
    }

    /**
     * Handles volunteer signup with business rules.
     */
    fun signupAsVolunteer(userId: String, campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.volunteerSignup(userId, campaignId).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isVolunteering = true,
                        uiMessage = "Successfully signed up as a volunteer!"
                    )
                }
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        uiMessage = e.message ?: "Signup failed."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(uiMessage = null) }
    }
}
