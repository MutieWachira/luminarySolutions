package com.example.luminarysolutions.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.DashboardStats
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.AuthRepository
import com.example.luminarysolutions.data.repository.AuthStatus
import com.example.luminarysolutions.data.repository.ClientRepository
import com.example.luminarysolutions.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ClientDashboardUiState {
    object Loading : ClientDashboardUiState
    data class Success(
        val user: User?,
        val stats: DashboardStats,
        val ongoingProjects: List<Freelance>,
        val featuredServices: List<Freelance>
    ) : ClientDashboardUiState
    data class Error(val message: String) : ClientDashboardUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClientDashboardViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<ClientDashboardUiState> = authRepository.getAuthState()
        .flatMapLatest { authStatus ->
            when (authStatus) {
                is AuthStatus.Authenticated -> {
                    combine(
                        userRepository.getUserProfile(authStatus.uid),
                        clientRepository.getDashboardStats(authStatus.uid),
                        clientRepository.getClientProjects(authStatus.uid),
                        clientRepository.getFreelanceServices()
                    ) { user, stats, projects, allServices ->
                        // Filter out projects the client is already working on for "New Opportunities"
                        val ongoingProjectIds = projects.map { it.id }.toSet()
                        val availableServices = allServices.filter { it.id !in ongoingProjectIds }

                        ClientDashboardUiState.Success(
                            user = user,
                            stats = stats,
                            ongoingProjects = projects,
                            featuredServices = availableServices.take(5)
                        )
                    }
                }
                is AuthStatus.Error -> flowOf(ClientDashboardUiState.Error(authStatus.message))
                AuthStatus.Unauthenticated -> flowOf(ClientDashboardUiState.Error("User not authenticated"))
                AuthStatus.Loading -> flowOf(ClientDashboardUiState.Loading)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClientDashboardUiState.Loading
        )
}
