package com.example.luminarysolutions.ui.ceo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var totalProjects by mutableStateOf(0)
        private set

    var budgetUtilized by mutableStateOf(0)
        private set

    var impactScore by mutableStateOf(0)
        private set

    private var projectsListener: ListenerRegistration? = null
    private var financeListener: ListenerRegistration? = null

    init { fetchKPIs() }

    private fun fetchKPIs() {

        // 🔹 Listen to projects collection
        projectsListener = db.collection("projects")
            .addSnapshotListener { snapshot, _ ->
                totalProjects = snapshot?.size() ?: 0
            }

        // 🔹 Listen to finance summary
        financeListener = db.collection("finance").document("summary")
            .addSnapshotListener { doc, _ ->
                val totalBudget = doc?.getLong("total_budget") ?: 1
                val utilizedBudget = doc?.getLong("utilized_budget") ?: 0
                budgetUtilized = utilizedBudget.toInt()
                impactScore = ((utilizedBudget.toDouble() / totalBudget.toDouble()) * 100).toInt()
            }
    }

    override fun onCleared() {
        super.onCleared()
        projectsListener?.remove()
        financeListener?.remove()
    }
}
