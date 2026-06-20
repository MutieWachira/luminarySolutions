package com.example.luminarysolutions.ui.donor.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.ui.donor.data.DonorRepository
import com.example.luminarysolutions.ui.donor.data.DonorRepositoryImpl
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * UI State for the Donor Module.
 * Follows Industry Best Practices for state management in Compose.
 */
data class DonorUiState(
    val campaigns: List<CampaignUi> = emptyList(),
    val donations: List<DonationUi> = emptyList(),
    val reports: List<ImpactReportUi> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "All",
    val uiMessage: String? = null,
    val isVolunteering: Boolean = false,
    val isVolunteeringForCurrentCampaign: Boolean = false,
    val isVolunteerRole: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
    val error: String? = null
)

/**
 * Production-ready ViewModel for the Donor module.
 * Implements MVVM with Clean Architecture principles.
 */
class DonorViewModel(
    private val repo: DonorRepository = DonorRepositoryImpl()
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(DonorUiState())
    val uiState: StateFlow<DonorUiState> = _uiState.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")

    init {
        checkAuthStatus()
        observeCampaigns()
        observeReports()
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

    fun joinCampaign(campaignId: String) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.joinProject(userId, campaignId)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isVolunteering = result.isSuccess,
                    isVolunteeringForCurrentCampaign = result.isSuccess,
                    uiMessage = if (result.isSuccess) "Welcome to the team! You've successfully joined this project."
                                else result.exceptionOrNull()?.message ?: "Failed to join project."
                )
            }
        }
    }

    fun setCategory(category: String) {
        _categoryFilter.value = category
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
    }

    fun getCampaign(campaignId: String): Flow<CampaignUi?> = repo.getCampaign(campaignId)

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
            val result = repo.donate(userId, campaignId, amount)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    uiMessage = if (result.isSuccess) "Donation successful! Thank you for your support." 
                                else "Donation failed. Please try again."
                )
            }
        }
    }

    /**
     * Handles volunteer signup with business rules.
     */
    fun signupAsVolunteer(userId: String, campaignId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.volunteerSignup(userId, campaignId)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isVolunteering = result.isSuccess,
                    uiMessage = if (result.isSuccess) "Successfully signed up as a volunteer!"
                                else result.exceptionOrNull()?.message ?: "Signup failed."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(uiMessage = null) }
    }
}
