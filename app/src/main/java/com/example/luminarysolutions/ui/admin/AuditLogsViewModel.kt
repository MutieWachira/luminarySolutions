package com.example.luminarysolutions.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.AuditLog
import com.example.luminarysolutions.data.repository.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditLogsViewModel @Inject constructor(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<AuditLog>>(emptyList())
    val logs: StateFlow<List<AuditLog>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchLogs()
    }

    fun fetchLogs() {
        _isLoading.value = true
        viewModelScope.launch {
            auditLogRepository.getLatestLogs()
                .onSuccess { fetchedLogs ->
                    _logs.value = fetchedLogs
                }
                .onFailure {
                    // Error handling could be added here
                }
            _isLoading.value = false
        }
    }
}
