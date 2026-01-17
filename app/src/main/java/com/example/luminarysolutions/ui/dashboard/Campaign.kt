package com.example.luminarysolutions.ui.dashboard

data class Campaign(
    val id: String,
    val title: String,
    val description: String,
    val amountRaised: Int,
    val goalAmount: Int,
    val imageRes: Int
){
    val progress: Float
        get() = amountRaised.toFloat() / goalAmount.toFloat()
}
