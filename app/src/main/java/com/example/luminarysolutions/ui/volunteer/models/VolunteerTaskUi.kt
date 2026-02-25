package com.example.luminarysolutions.ui.volunteer.models

data class VolunteerTaskUi(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: String,
    val location: String,
    val lastUpdate: String
)

enum class TaskStatus { ASSIGNED, IN_PROGRESS, DONE }