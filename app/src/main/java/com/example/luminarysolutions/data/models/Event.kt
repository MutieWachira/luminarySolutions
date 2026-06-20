package com.example.luminarysolutions.data.models

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val imageUrl: String? = null,
    val projectId: String? = null, // Optional: link to a project/program
    val attendees: List<String> = emptyList(), // List of user IDs (Volunteers/Teams)
    val type: String = "General", // General, Workshop, Fundraising, Volunteer Meeting
    val createdAt: Long = System.currentTimeMillis()
)
