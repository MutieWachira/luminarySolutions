package com.example.luminarysolutions.data.models

data class Donor(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val valueOrNote: String,
    val lastContact: String
)