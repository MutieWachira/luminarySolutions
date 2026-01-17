package com.example.luminarysolutions.ui.home

data class Campaign(
    val id: String,
    val title: String,
    val description: String,
    val amountRaised: Int,
    val goalAmount: Int
)