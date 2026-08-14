package com.example.luminarysolutions.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.SystemSettings
import com.example.luminarysolutions.data.repository.AdminRepository
import com.example.luminarysolutions.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemSettingsState(
    val settings: SystemSettings = SystemSettings(),
    val isLoading: Boolean = false,
    val message: String? = null
)

/**
 * ViewModel for System Settings.
 * Handles platform configuration and administrative actions like logout.
 */
@HiltViewModel
class SystemSettingsViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SystemSettingsState())
    val state: StateFlow<SystemSettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads system configuration from the repository.
     */
    private fun loadSettings() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            adminRepository.getSystemSettings()
                .onSuccess { settings ->
                    _state.value = _state.value.copy(settings = settings, isLoading = false)
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, message = "Failed to load settings")
                }
        }
    }

    fun updateMfa(required: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(mfaRequired = required))
    }

    fun updateSessionTimeout(minutes: Int) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(sessionTimeout = minutes))
    }

    fun updateMaintenanceMode(enabled: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(maintenanceMode = enabled))
    }

    fun updateMinPasswordLength(length: Int) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(minPasswordLength = length))
    }

    fun updateAuditLogging(enabled: Boolean) {
        _state.value = _state.value.copy(settings = _state.value.settings.copy(auditLogging = enabled))
    }

    /**
     * Persists the current settings state to the backend.
     */
    fun saveSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            adminRepository.saveSystemSettings(_state.value.settings)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, message = "Settings saved successfully")
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, message = "Failed to save settings")
                }
        }
    }

    /**
     * Logs out the current user by clearing the authentication session.
     */
    fun logout() {
        authRepository.signOut()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
