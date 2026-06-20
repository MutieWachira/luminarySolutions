package com.example.luminarysolutions.ui.auth

enum class UserRole {
    CEO,
    ADMIN,
    IT_ADMIN,
    VOLUNTEER,
    DONOR,
    TEAM,
    UNKNOWN
}

// A safe way to convert a String to a UserRole
fun safeValueOf(roleString: String?): UserRole {
    val normalized = roleString
        ?.trim()
        ?.uppercase()
        ?.replace(" ", "_")   // "IT ADMIN" -> "IT_ADMIN"
        ?: return UserRole.UNKNOWN

    return when (normalized) {
        "CEO" -> UserRole.CEO
        "ADMIN" -> UserRole.ADMIN
        "IT_ADMIN" -> UserRole.IT_ADMIN
        "VOLUNTEER" -> UserRole.VOLUNTEER
        "DONOR" -> UserRole.DONOR
        "TEAM" -> UserRole.TEAM
        else -> UserRole.UNKNOWN
    }
}