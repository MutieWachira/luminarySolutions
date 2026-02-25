package com.example.luminarysolutions.ui.donor.models

data class DonationUi(
    val id: String,
    val campaignTitle: String,
    val amount: Int,
    val status: String, // "Successful", "Pending", "Failed"
    val date: String,
    val receiptRef: String
)