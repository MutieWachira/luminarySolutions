package com.example.luminarysolutions.data.models

/**
 * Model representing a service acquisition request from a client.
 */
data class ServiceAcquisition(
    val id: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val price: String = "",
    val status: String = "Requested", // Requested, Approved, In Progress, Completed
    val createdAt: Long = System.currentTimeMillis()
)
