package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.repository.PartnersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State for the Partners Screen
 */
data class PartnerUiState(
    val partners: List<Partner> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isSaving: Boolean = false
)

/**
 * ViewModel for managing Partners business logic and state
 */
class PartnersViewModel : ViewModel() {
    private val repository = PartnersRepository()
    private val _searchQuery = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)

    // combine the repository flow with search query for real-time filtering
    val uiState: StateFlow<PartnerUiState> = combine(
        repository.getPartners(),
        _searchQuery,
        _isSaving
    ) { partners, query, isSaving ->
        val filtered = partners.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.type.contains(query, ignoreCase = true)
        }
        PartnerUiState(
            partners = filtered,
            isLoading = false,
            searchQuery = query,
            isSaving = isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PartnerUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addPartner(name: String, type: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val newPartner = Partner(
                id = "",
                name = name,
                type = type,
                status = "Active",
                valueOrNote = "",
                lastContact = ""
            )
            repository.addPartner(newPartner) { success ->
                _isSaving.value = false
            }
        }
    }
}
