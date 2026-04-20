package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Donor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DonorsUiState(
    val donors: List<Donor> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filter: DonorFilter = DonorFilter.ALL
)

enum class DonorFilter { ALL, ACTIVE, PENDING, INACTIVE }

class DonorsViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(DonorFilter.ALL)
    
    val uiState: StateFlow<DonorsUiState> = combine(
        FirestoreService.getDonors(),
        _searchQuery,
        _filter
    ) { donors, query, filter ->
        val filteredDonors = donors.filter { donor ->
            val matchesQuery = donor.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                DonorFilter.ALL -> true
                DonorFilter.ACTIVE -> donor.status == "Active"
                DonorFilter.PENDING -> donor.status == "Pending"
                DonorFilter.INACTIVE -> donor.status == "Inactive"
            }
            matchesQuery && matchesFilter
        }
        DonorsUiState(
            donors = filteredDonors,
            isLoading = false,
            searchQuery = query,
            filter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DonorsUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: DonorFilter) {
        _filter.value = filter
    }

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
            FirestoreService.addDonor(newDonor) { success ->
                // Handle result if needed
            }
        }
    }
}
