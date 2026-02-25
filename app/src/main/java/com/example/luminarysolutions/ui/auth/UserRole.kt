package com.example.luminarysolutions.ui.auth

enum class UserRole {
    CEO,
    ADMIN,
    VOLUNTEER,
    DONOR,
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
        "VOLUNTEER" -> UserRole.VOLUNTEER
        "DONOR" -> UserRole.DONOR
        else -> UserRole.UNKNOWN
    }
}