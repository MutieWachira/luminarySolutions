package com.example.luminarysolutions.ui.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.repository.CampaignRepository
import com.example.luminarysolutions.data.repository.CampaignRepositoryImpl
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.CategoryUi
import com.example.luminarysolutions.ui.donor.models.HeroItemUi
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * UI State for the Donor Dashboard.
 * Includes statistics for social proof and impact tracking.
 */
data class DonorDashboardUiState(
    val isLoading: Boolean = true,
    val isPaginating: Boolean = false,
    val heroItems: List<HeroItemUi> = emptyList(),
    val categories: List<CategoryUi> = emptyList(),
    val campaigns: List<CampaignUi> = emptyList(),
    val selectedCategory: String = "All",
    val totalImpactReached: String = "0",
    val totalRaised: String = "0",
    val activeCampaignsCount: String = "0",
    val hasMore: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Donor Dashboard following MVVM patterns.
 * Handles data orchestration, state preservation, and pagination.
 */
class DonorDashboardViewModel(
    private val repository: CampaignRepository = CampaignRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonorDashboardUiState())
    val uiState: StateFlow<DonorDashboardUiState> = _uiState.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 6

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Industry Best Practice: Parallel execution for faster load times
                val categoriesDeferred = async { repository.getCategories().first() }
                val heroItemsDeferred = async { repository.getHeroItems().first() }
                val statsDeferred = async { repository.getDashboardStats().first() }
                val campaignsDeferred = async { 
                    repository.getCampaigns(
                        category = _uiState.value.selectedCategory,
                        pageSize = pageSize
                    )
                }

                val categories = categoriesDeferred.await()
                val heroItems = heroItemsDeferred.await()
                val stats = statsDeferred.await()
                val campaignsResult = campaignsDeferred.await()

                if (campaignsResult.isSuccess) {
                    val pagedResult = campaignsResult.getOrThrow()
                    lastDocument = pagedResult.lastDocument

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories,
                        heroItems = heroItems,
                        totalImpactReached = stats.first,
                        activeCampaignsCount = stats.second,
                        totalRaised = stats.third,
                        campaigns = pagedResult.data,
                        hasMore = pagedResult.hasMore,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = campaignsResult.exceptionOrNull()?.message ?: "Failed to load campaigns"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load dashboard data"
                )
            }
        }
    }

    fun onCategorySelected(category: String) {
        if (_uiState.value.selectedCategory == category) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedCategory = category, isLoading = true, campaigns = emptyList(), error = null)
            lastDocument = null
            
            val result = repository.getCampaigns(
                category = category,
                pageSize = pageSize
            )
            
            if (result.isSuccess) {
                val pagedResult = result.getOrThrow()
                lastDocument = pagedResult.lastDocument
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    campaigns = pagedResult.data,
                    hasMore = pagedResult.hasMore
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to filter campaigns"
                )
            }
        }
    }

    fun loadMoreCampaigns() {
        if (_uiState.value.isPaginating || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPaginating = true, error = null)
            
            val result = repository.getCampaigns(
                category = _uiState.value.selectedCategory,
                pageSize = pageSize,
                lastDocument = lastDocument
            )

            if (result.isSuccess) {
                val pagedResult = result.getOrThrow()
                lastDocument = pagedResult.lastDocument
                _uiState.value = _uiState.value.copy(
                    isPaginating = false,
                    campaigns = _uiState.value.campaigns + pagedResult.data,
                    hasMore = pagedResult.hasMore
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isPaginating = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load more"
                )
            }
        }
    }

    fun retry() {
        loadInitialData()
    }
}
