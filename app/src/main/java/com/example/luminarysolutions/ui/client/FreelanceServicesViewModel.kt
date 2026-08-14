package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Freelance Services screen.
 */
data class FreelanceServicesUiState(
    val services: List<Freelance> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val categories: List<String> = emptyList()
)

/**
 * ViewModel for browsing and interacting with Luminary's freelance services.
 */
@HiltViewModel
class FreelanceServicesViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<FreelanceServicesUiState> = combine(
        repository.getFreelanceServices(),
        _searchQuery,
        _selectedCategory
    ) { services, query, category ->
        val filtered = services.filter { service ->
            val matchesQuery = service.name.contains(query, ignoreCase = true) || 
                               service.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || service.category == category
            matchesQuery && matchesCategory
        }

        val allCategories = listOf("All") + services.map { it.category }.distinct().sorted()

        FreelanceServicesUiState(
            services = filtered,
            isLoading = false,
            searchQuery = query,
            selectedCategory = category,
            categories = allCategories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FreelanceServicesUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun applyForService(serviceId: String, clientId: String) {
        viewModelScope.launch {
            repository.applyForService(serviceId, clientId).onSuccess {
                // Handle success (e.g., show a toast or update UI state)
            }
        }
    }
}
