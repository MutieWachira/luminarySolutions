package com.example.luminarysolutions.data.models

/**
 * Model representing an inquiry sent by a client regarding a specific service.
 */
data class Enquiry(
    val id: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "Pending", // Pending, Responded, Closed
    val createdAt: Long = System.currentTimeMillis()
)
