package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.FirestoreService
import kotlinx.coroutines.flow.Flow

class DashboardRepository {
    fun getDashboardStats(): Flow<DashboardStats> = FirestoreService.getDashboardStats()
}
