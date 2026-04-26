package com.example.luminarysolutions.data.models

data class Approval(
    val id: String = "",
    val type: String = "",
    val account: String = "",
    val amount: Int = 0,
    val priority: String = "Medium",
    val date: String = "",
    val status: String = "Pending", // Pending, Approved, Rejected
    val requestedBy: String = ""
)
