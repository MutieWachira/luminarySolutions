package com.example.luminarysolutions.data.models

data class Volunteer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val skills: List<String> = emptyList(),
    val motivation: String = "",
    val status: String = "Pending", // Pending, Approved, Rejected
    val appliedDate: Long = System.currentTimeMillis(),
    val projectIds: List<String> = emptyList(), // Supports multiple signups
    
    // Gamification
    val points: Int = 0,
    val level: Int = 1,
    val trophiesCount: Int = 0,
    val achievements: List<String> = emptyList(), // IDs of unlocked achievements
    
    // Settings
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
)
