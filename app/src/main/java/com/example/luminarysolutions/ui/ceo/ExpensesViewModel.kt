package com.example.luminarysolutions.ui.ceo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.Expense
import com.example.luminarysolutions.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Expenses screen, following MVVM and Clean Architecture principles.
 * Uses a reactive approach to handle data updates and filtering.
 */
@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {
    private val _projectId = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    // Reactive UI state that automatically updates when expenses or project ID change
    val uiState: StateFlow<ExpensesUiState> = _projectId
        .flatMapLatest { projectId ->
            repository.getExpenses()
                .map { expenses ->
                    val filteredExpenses = if (projectId != null) {
                        expenses.filter { it.projectId == projectId }
                    } else {
                        expenses
                    }
                    ExpensesUiState(expenses = filteredExpenses, isLoading = false)
                }
                .onStart { emit(ExpensesUiState(isLoading = true)) }
                .catch { e -> emit(ExpensesUiState(error = e.message)) }
        }
        .combine(_error) { state, error ->
            state.copy(error = error)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpensesUiState(isLoading = true)
        )

    /**
     * Sets the current project ID to filter expenses.
     */
    fun setProjectId(projectId: String?) {
        _projectId.value = projectId
    }

    /**
     * Adds a new expense to Firestore.
     */
    fun addExpense(category: String, account: String, amount: Int) {
        val newExpense = Expense(
            category = category,
            account = account,
            amount = amount,
            projectId = _projectId.value // Associate with current project if applicable
        )
        
        viewModelScope.launch {
            repository.addExpense(newExpense)
                .onFailure {
                    _error.value = "Failed to record expense. Please try again."
                }
        }
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _error.value = null
    }
}
