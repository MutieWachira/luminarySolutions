package com.example.luminarysolutions.ui.ceo.models

data class ProjectUi(
    val id: String,
    val name: String,
    val status: String,
    val budget: Int,
    val spent: Int,
    val progress: Float,
    val lastUpdated: String
)
