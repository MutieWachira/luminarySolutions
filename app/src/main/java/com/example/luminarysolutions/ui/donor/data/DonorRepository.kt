package com.example.luminarysolutions.ui.donor.data

import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi

interface DonorRepository {
    fun getCampaigns(): List<CampaignUi>
    fun getCampaign(campaignId: String): CampaignUi?
    fun getMyDonations(userId: String): List<DonationUi>
    fun getReports(): List<ImpactReportUi>

    // UI-only for now (later connect to payment gateway + Firestore)
    fun donate(userId: String, campaignId: String, amount: Int): DonationUi
}