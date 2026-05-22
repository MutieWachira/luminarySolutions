package com.example.luminarysolutions.data.models

/**
 * Model representing Team Culture metrics.
 * Part of Clean Architecture: Domain Model.
 */
data class TeamCulture(
    val diversityRate: String = "38%",
    val satisfactionScore: String = "4.6/5",
    val trainingPrograms: Int = 24
)
