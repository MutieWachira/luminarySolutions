package com.example.luminarysolutions.data.models

data class Project(
    val id: String,
    val name: String,
    val status: String,
    val budget: Int,
    val spent: Int,
    val progress: Float,
    val lastUpdated: String
)
