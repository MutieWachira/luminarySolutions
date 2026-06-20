package com.example.luminarysolutions.data.models

data class Document(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val uploader: String = "",
    val date: String = "",
    val size: String = "",
    val fileUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
