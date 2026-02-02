package com.example.luminarysolutions.ui.auth

enum class UserRole {
    CEO,
    IT_ADMIN,
    VOLUNTEER,
    DONOR,
    UNKNOWN
}

// A safe way to convert a String to a UserRole
fun safeValueOf(role: String?): UserRole {
    return try {
        // Trim whitespace and convert to uppercase to make matching more robust
        UserRole.valueOf(role?.trim()?.uppercase() ?: "")
    } catch (e: IllegalArgumentException) {
        // Default to UNKNOWN if the role from Firestore is invalid
        UserRole.UNKNOWN
    }
}