package com.example.luminarysolutions.data.models

import java.util.UUID

enum class AssigneeType {
    TEAM, VOLUNTEER
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val assignedToIds: List<String> = emptyList(),
    val assignedToNames: List<String> = emptyList(),
    val assignedById: String = "",
    val assigneeType: AssigneeType = AssigneeType.TEAM,
    val deadline: Long = System.currentTimeMillis() + (86400000 * 7), // Default 1 week
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // For backward compatibility during migration
    val assignedToId: String get() = assignedToIds.firstOrNull() ?: ""
    val assignedToName: String get() = assignedToNames.firstOrNull() ?: ""
}

data class Project(
    val id: String = "",
    val name: String = "",
    val status: String = "Ongoing",
    val budget: Int = 0,
    val spent: Int = 0,
    val progress: Float = 0f,
    val lastUpdated: String = "",
    val imageUrl: String? = null,
    val description: String = "",
    val location: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val tasks: List<Task> = emptyList(),
    val volunteers: List<String> = emptyList(),
    val groupLeaderIds: List<String> = emptyList(),
    val teamMemberIds: List<String> = emptyList(),
    val clients: List<String> = emptyList(),
    val groupLeaderId: String = "",
    val client: String = "",
    val category: String = ""
) {
    /**
     * Calculates project progress based on completed tasks.
     */
    val calculatedProgress: Float
        get() = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
}
