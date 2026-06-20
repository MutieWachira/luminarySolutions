package com.example.luminarysolutions.ui.ceo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeamUiState {
    object Idle : TeamUiState()
    object Loading : TeamUiState()
    data class Success(val message: String) : TeamUiState()
    data class Error(val message: String) : TeamUiState()
}

class TeamManagementViewModel(
    private val repository: TeamRepository = TeamRepository()
) : ViewModel() {

    var uiState by mutableStateOf<TeamUiState>(TeamUiState.Idle)
        private set

    private val _teamMembers = MutableStateFlow<List<Team>>(emptyList())
    val teamMembers: StateFlow<List<Team>> = _teamMembers

    init {
        fetchTeamMembers()
    }

    fun fetchTeamMembers() {
        viewModelScope.launch {
            _teamMembers.value = repository.getTeamMembers()
        }
    }

    fun addTeamMember(
        name: String,
        email: String,
        phone: String,
        department: String,
        jobTitle: String
    ) {
        if (name.isBlank() || email.isBlank()) {
            uiState = TeamUiState.Error("Name and Email are required")
            return
        }

        uiState = TeamUiState.Loading

        val newTeam = Team(
            name = name,
            email = email,
            phone = phone,
            department = department,
            jobtitle = jobTitle
        )

        viewModelScope.launch {
            val result = repository.addTeamMember(newTeam)
            if (result.isSuccess) {
                uiState = TeamUiState.Success("Team member added successfully. Email sent.")
                fetchTeamMembers()
            } else {
                uiState = TeamUiState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }

    fun clearState() {
        uiState = TeamUiState.Idle
    }

    fun testSmtp() {
        uiState = TeamUiState.Loading
        viewModelScope.launch {
            val result = repository.testSmtp()
            uiState = if (result.isSuccess) {
                TeamUiState.Success("SMTP Test: ${result.getOrNull()}")
            } else {
                TeamUiState.Error("SMTP Test Failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
