package com.example.luminarysolutions.data.models

/**
 * Modern Freelance model for Luminary Solutions.
 * Represents a service or gig that clients can apply for.
 */
data class Freelance(
    val id: String = "",
    val imageUrl: String? = null,
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "Pending",
    val teamIds: List<String> = emptyList(), // Team members assigned by CEO
    val clientIds: List<String> = emptyList(), // Clients who applied for the service
    val tasks: List<Task> = emptyList(),
    val progress: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculates freelance progress based on completed tasks.
     */
    val calculatedProgress: Float
        get() = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
}
