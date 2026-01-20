package com.example.luminarysolutions.ui.dashboard


import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.luminarysolutions.R


class CampaignDashboardViewModel : ViewModel() {
    private val _campaigns = mutableStateOf<List<Campaign>>(emptyList())
    val campaigns: State<List<Campaign>> = _campaigns

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init{
        loadCampaigns()
    }

    private fun loadCampaigns(){
        _isLoading.value = true

        //simulated backend data
        _campaigns.value = listOf(
            Campaign(
                id = "1",
                title = "Help Build a School",
                description = "Raise funds to build a school in rural areas",
                amountRaised = 45000,
                goalAmount = 100000,
                imageRes=R.drawable.schoolcampaign
            ),
            Campaign(
                id = "2",
                title = "Medical Support Fund",
                description = "Supporting emergency medical treatments",
                amountRaised = 49000,
                goalAmount = 50000,
                imageRes= R.drawable.medicalcampaign
            ),
            Campaign(
                id = "3",
                title = "Medical Support Fund",
                description = "Supporting emergency medical treatments",
                amountRaised = 49000,
                goalAmount = 50000,
                imageRes= R.drawable.medicalcampaign
            ),
            Campaign(
                id = "4",
                title = "Medical Support Fund",
                description = "Supporting emergency medical treatments",
                amountRaised = 49000,
                goalAmount = 50000,
                imageRes= R.drawable.medicalcampaign
            )
        )
        _isLoading.value = false
    }

}