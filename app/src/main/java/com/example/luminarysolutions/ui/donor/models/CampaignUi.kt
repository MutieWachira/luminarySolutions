package com.example.luminarysolutions.ui.donor.models

data class CampaignUi(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val goalAmount: Int,
    val raisedAmount: Int,
    val lastUpdate: String
)