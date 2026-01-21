package com.example.luminarysolutions.ui.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.luminarysolutions.R

class CampaignDetailsViewModel : ViewModel() {

    private val _campaign = mutableStateOf<Campaign?>(null)
    val campaign: State<Campaign?> = _campaign

    fun loadCampaign(campaignId: String) {
        // Fake data for now (later repository)
        _campaign.value = Campaign(
            id = campaignId,
            title = "Help Build a School",
            description = "Support education by helping us build a school for underprivileged children.",
            imageRes = R.drawable.schoolcampaign,
            amountRaised = 45000,
            goalAmount = 100000
        )
    }
}
