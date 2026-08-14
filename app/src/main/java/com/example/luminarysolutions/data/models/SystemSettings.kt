package com.example.luminarysolutions.data.models

data class SystemSettings(
    val mfaRequired: Boolean = true,
    val sessionTimeout: Int = 15,
    val maintenanceMode: Boolean = false,
    val minPasswordLength: Int = 10,
    val auditLogging: Boolean = true
)
