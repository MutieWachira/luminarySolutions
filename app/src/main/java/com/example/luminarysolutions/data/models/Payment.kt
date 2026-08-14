package com.example.luminarysolutions.data.models

import com.google.firebase.Timestamp

data class Payment(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val phoneNumber: String = "",
    val status: String = "Pending", // Pending, Completed, Failed
    val checkoutRequestId: String = "",
    val mpesaReceiptNumber: String? = null,
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val reference: String = ""
)

data class MpesaRequest(
    val amount: Double,
    val phoneNumber: String,
    val userId: String,
    val reference: String? = null
)

data class MpesaResponse(
    val success: Boolean,
    val checkoutRequestId: String? = null,
    val customerMessage: String? = null
)
