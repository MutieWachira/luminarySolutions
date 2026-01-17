package com.example.luminarysolutions.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class HomeViewModel : ViewModel() {

    private val _campaigns = mutableStateOf (
        listOf(
            Campaign(
                id = "1",
                title = "Help Build a School",
                description = "Raising funds to build a school in a rural area.",
                amountRaised = 45000,
                goalAmount = 100000
            ),
            Campaign(
                id = "2",
                title = "Medical Support for Jane",
                description = "Support Jane’s surgery and recovery.",
                amountRaised = 120000,
                goalAmount = 150000
            )
        )
    )

    val campaigns: State<List<Campaign>> = _campaigns
}