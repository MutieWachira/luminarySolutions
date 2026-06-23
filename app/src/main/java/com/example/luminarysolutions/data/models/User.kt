package com.example.luminarysolutions.data.models

import com.example.luminarysolutions.ui.auth.UserRole

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val profileImageUrl: String? = null,
    val role: UserRole = UserRole.TEAM,
    val enabled: Boolean = true,
    val fcmToken: String? = null,
    val isTwoFactorEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)
