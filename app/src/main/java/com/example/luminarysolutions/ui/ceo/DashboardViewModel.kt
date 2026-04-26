package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.repository.DashboardRepository
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Approval
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CEODashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val initiatives: List<Project> = emptyList(),
    val approvals: List<Approval> = emptyList(),
    val isLoading: Boolean = true
)

class CEODashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    val uiState: StateFlow<CEODashboardUiState> = combine(
        repository.getDashboardStats(),
        repository.getOngoingInitiatives(),
        repository.getRecentApprovals()
    ) { stats, initiatives, approvals ->
        CEODashboardUiState(
            stats = stats,
            initiatives = initiatives,
            approvals = approvals,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CEODashboardUiState()
    )
}
