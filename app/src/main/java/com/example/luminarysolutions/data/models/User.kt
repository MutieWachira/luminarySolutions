package com.example.luminarysolutions.data.models

import com.example.luminarysolutions.ui.auth.UserRole

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.TEAM,
    val enabled: Boolean = true
)
