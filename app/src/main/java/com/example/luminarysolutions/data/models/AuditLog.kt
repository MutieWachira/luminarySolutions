package com.example.luminarysolutions.data.models

import java.util.Date

data class AuditLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Unknown",
    val action: String = "",
    val timestamp: Date = Date(),
    val details: String = "",
    val severity: String = "INFO"
)
