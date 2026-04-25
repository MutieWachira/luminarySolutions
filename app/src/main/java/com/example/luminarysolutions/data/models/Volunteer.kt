package com.example.luminarysolutions.data.models

data class Volunteer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val skills: List<String> = emptyList(),
    val status: String = "Available"
)
