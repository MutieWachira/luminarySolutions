package com.example.luminarysolutions.data.models

data class Expense(
    val id: String = "",
    val category: String = "",
    val account: String = "",
    val amount: Int = 0,
    val date: String= "",
    val timestamp: Long = System.currentTimeMillis()
)