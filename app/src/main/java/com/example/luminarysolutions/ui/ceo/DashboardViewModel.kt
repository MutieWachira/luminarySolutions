package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.repository.DashboardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CEODashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val isLoading: Boolean = true
)

class CEODashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    val uiState: StateFlow<CEODashboardUiState> = repository.getDashboardStats()
        .map { stats ->
            CEODashboardUiState(stats = stats, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CEODashboardUiState()
        )
}
