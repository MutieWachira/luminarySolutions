package com.example.luminarysolutions.data.models

data class Donor(
    val id: String = "",
    val name: String = "",
    val type: String = "Donor",
    val status: String = "Active",
    val valueOrNote: String = "",
    val lastContact: String = "",
    val points: Int = 0,
    val level: Int = 1,
    val achievements: List<String> = emptyList(),
    val totalDonated: Int = 0,
    val donationCount: Int = 0
)
