package com.example.luminarysolutions.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.AdminRepository
import com.example.luminarysolutions.ui.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _roleFilter = MutableStateFlow<UserRole?>(null)
    val roleFilter = _roleFilter.asStateFlow()

    val users: StateFlow<List<User>> = combine(
        adminRepository.getAllUsers(),
        _searchQuery,
        _roleFilter
    ) { allUsers, query, role ->
        allUsers.filter { user ->
            val matchesQuery = user.name.contains(query, ignoreCase = true) || 
                               user.email.contains(query, ignoreCase = true)
            val matchesRole = role == null || user.role == role
            matchesQuery && matchesRole
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateRoleFilter(role: UserRole?) {
        _roleFilter.value = role
    }

    fun toggleUserEnabled(user: User) {
        viewModelScope.launch {
            adminRepository.setUserEnabled(user.id, !user.enabled)
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to update user status"
                }
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            adminRepository.updateUserRole(userId, newRole)
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to update user role"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
