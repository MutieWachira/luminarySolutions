package com.example.luminarysolutions.data.models

import com.example.luminarysolutions.ui.auth.UserRole

data class Team(
    val id: String = "",
    val imageUrl: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val jobtitle: String = "",
    val gender: String = "",
    val bio: String = "",
    val role: UserRole = UserRole.TEAM,
    val enabled: Boolean = true,
    val isTwoFactorEnabled: Boolean = false,
    val datejoined: Long = System.currentTimeMillis()
)
