package com.example.luminarysolutions.data.models

data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String? = null,
    val pointsAwarded: Int = 0,
    val criteriaType: String = "", // e.g., "TASKS_COMPLETED", "HOURS_VOLUNTEERED"
    val criteriaValue: Int = 0
)

data class VolunteerAchievement(
    val id: String = "",
    val volunteerId: String = "",
    val achievementId: String = "",
    val unlockedAt: Long = System.currentTimeMillis()
)
