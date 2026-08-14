package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.repository.DashboardRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DonorsUiState(
    val donors: List<Donor> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val searchQuery: String = "",
    val filter: DonorFilter = DonorFilter.ALL,
    val error: String? = null,
    val canLoadMore: Boolean = true
)

enum class DonorFilter { ALL, ACTIVE, PENDING, INACTIVE }

/**
 * ViewModel for the Donors screen.
 * Follows Clean Architecture by keeping logic reactive and state-driven.
 * Implements MVVM pattern with repository-based data fetching and pagination.
 */
@HiltViewModel
class DonorsViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(DonorFilter.ALL)
    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 10L

    private val _uiState = MutableStateFlow(DonorsUiState(isLoading = true))
    val uiState: StateFlow<DonorsUiState> = _uiState.asStateFlow()

    init {
        loadDonors()
    }

    private var loadDonorsJob: kotlinx.coroutines.Job? = null

    /**
     * Initial load of donors.
     */
    private fun loadDonors() {
        loadDonorsJob?.cancel()
        loadDonorsJob = viewModelScope.launch {
            combine(
                repository.getDonorsPaginated(null, pageSize),
                _searchQuery,
                _filter
            ) { paginatedData, query, filter ->
                val (donors, lastDoc) = paginatedData
                lastDocument = lastDoc
                
                val filteredDonors = filterDonors(donors, query, filter)
                
                DonorsUiState(
                    donors = filteredDonors,
                    isLoading = false,
                    searchQuery = query,
                    filter = filter,
                    canLoadMore = donors.size >= pageSize
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    /**
     * Loads the next page of donors.
     */
    fun loadNextPage() {
        if (_uiState.value.isPaginating || !_uiState.value.canLoadMore) return

        _uiState.update { it.copy(isPaginating = true) }
        
        viewModelScope.launch {
            repository.getDonorsPaginated(lastDocument, pageSize).take(1).collect { paginatedData ->
                val (newDonors, lastDoc) = paginatedData
                lastDocument = lastDoc
                
                _uiState.update { currentState ->
                    val updatedList = (currentState.donors + newDonors).distinctBy { it.id }
                    currentState.copy(
                        donors = updatedList,
                        isPaginating = false,
                        canLoadMore = newDonors.size >= pageSize
                    )
                }
            }
        }
    }

    private fun filterDonors(donors: List<Donor>, query: String, filter: DonorFilter): List<Donor> {
        return donors.filter { donor ->
            val matchesQuery = donor.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                DonorFilter.ALL -> true
                DonorFilter.ACTIVE -> donor.status == "Active"
                DonorFilter.PENDING -> donor.status == "Pending"
                DonorFilter.INACTIVE -> donor.status == "Inactive"
            }
            matchesQuery && matchesFilter
        }
    }

    /**
     * Updates the search query for filtering donors.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Updates the active filter for donors.
     */
    fun onFilterChange(filter: DonorFilter) {
        _filter.value = filter
    }

    /**
     * Adds a new donor to the system.
     */
    fun addDonor(name: String, status: String, value: String) {
        viewModelScope.launch {
            val newDonor = Donor(
                id = "",
                name = name,
                type = "Donor",
                status = status,
                valueOrNote = value,
                lastContact = ""
            )
            repository.addDonor(newDonor).onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Failed to add donor") }
            }
        }
    }

    /**
     * Updates an existing donor.
     */
    fun updateDonor(donor: Donor) {
        viewModelScope.launch {
            repository.updateDonor(donor).onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Failed to update donor") }
            }
        }
    }

    /**
     * Deletes a donor.
     */
    fun deleteDonor(donorId: String) {
        viewModelScope.launch {
            repository.deleteDonor(donorId).onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Failed to delete donor") }
            }
        }
    }
}
