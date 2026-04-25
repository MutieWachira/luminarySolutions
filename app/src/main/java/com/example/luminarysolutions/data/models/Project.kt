package com.example.luminarysolutions.data.models

data class Task(
    val id: String = "",
    val title: String = "",
    val assignedTo: String = "",
    val isDone: Boolean = false
)

data class Project(
    val id: String,
    val name: String,
    val status: String,
    val budget: Int,
    val spent: Int,
    val progress: Float,
    val lastUpdated: String,
    val imageUrl: String? = null,
    val description: String = "",
    val location: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val tasks: List<Task> = emptyList(),
    val volunteers: List<String> = emptyList(), // List of volunteer IDs
    val groupLeaderId: String? = null
)
