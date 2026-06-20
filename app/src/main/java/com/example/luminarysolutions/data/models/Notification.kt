package com.example.luminarysolutions.data.models

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO", // INFO, SUCCESS, WARNING, ACHIEVEMENT
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
