package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ClientDashboardUiState(
    val recentServices: List<Freelance> = emptyList(),
    val ongoingProjectsCount: Int = 0,
    val pendingApplicationsCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ClientDashboardViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {

    val uiState: StateFlow<ClientDashboardUiState> = repository.getFreelanceServices()
        .map { services ->
            ClientDashboardUiState(
                recentServices = services.take(3),
                ongoingProjectsCount = services.count { it.status == "In Progress" },
                pendingApplicationsCount = services.count { it.status == "Pending" },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClientDashboardUiState()
        )
}
