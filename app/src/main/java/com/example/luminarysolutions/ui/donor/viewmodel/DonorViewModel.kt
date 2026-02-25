package com.example.luminarysolutions.ui.donor.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.luminarysolutions.ui.donor.data.InMemoryDonorRepository
import com.example.luminarysolutions.ui.donor.data.DonorRepository
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi

class DonorViewModel(
    private val repo: DonorRepository = InMemoryDonorRepository()
) : ViewModel() {

    var campaigns by mutableStateOf<List<CampaignUi>>(emptyList())
        private set

    var donations by mutableStateOf<List<DonationUi>>(emptyList())
        private set

    var reports by mutableStateOf<List<ImpactReportUi>>(emptyList())
        private set

    var uiMessage by mutableStateOf<String?>(null)
        private set

    fun load(userId: String) {
        campaigns = repo.getCampaigns()
        donations = repo.getMyDonations(userId)
        reports = repo.getReports()
    }

    fun getCampaign(campaignId: String): CampaignUi? = repo.getCampaign(campaignId)

    fun donate(userId: String, campaignId: String, amount: Int) {
        if (amount <= 0) {
            uiMessage = "Enter a valid amount."
            return
        }
        repo.donate(userId, campaignId, amount)
        load(userId)
        uiMessage = "Donation successful. Thank you!"
    }

    fun clearMessage() { uiMessage = null }
}