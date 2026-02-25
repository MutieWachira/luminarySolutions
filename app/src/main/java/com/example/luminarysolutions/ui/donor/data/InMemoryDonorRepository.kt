package com.example.luminarysolutions.ui.donor.data

import androidx.compose.runtime.mutableStateListOf
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import java.util.UUID

class InMemoryDonorRepository : DonorRepository {

    private val campaigns = mutableStateListOf(
        CampaignUi(UUID.randomUUID().toString(), "Clean Water Initiative", "Water", "Kibera", 200000, 120000, "2 days ago"),
        CampaignUi(UUID.randomUUID().toString(), "Youth Skills Program", "Education", "Mathare", 150000, 65000, "Today"),
        CampaignUi(UUID.randomUUID().toString(), "Community Health Outreach", "Health", "Kayole", 300000, 210000, "Yesterday")
    )

    private val donations = mutableStateListOf(
        DonationUi(UUID.randomUUID().toString(), "Clean Water Initiative", 5000, "Successful", "Feb 20, 2026", "RCP-001"),
        DonationUi(UUID.randomUUID().toString(), "Youth Skills Program", 2000, "Successful", "Feb 22, 2026", "RCP-002")
    )

    private val reports = mutableStateListOf(
        ImpactReportUi(UUID.randomUUID().toString(), "Q1 Impact Report", "Jan–Mar", "Apr 05, 2026",
            "Highlights: water access improved, skills training delivered, community outreach expanded."),
        ImpactReportUi(UUID.randomUUID().toString(), "Mid-Year Transparency Brief", "Jan–Jun", "Jul 10, 2026",
            "Summary: spending categories, milestone progress, and community feedback trends.")
    )

    override fun getCampaigns(): List<CampaignUi> = campaigns

    override fun getCampaign(campaignId: String): CampaignUi? =
        campaigns.firstOrNull { it.id == campaignId }

    override fun getMyDonations(userId: String): List<DonationUi> = donations

    override fun getReports(): List<ImpactReportUi> = reports

    override fun donate(userId: String, campaignId: String, amount: Int): DonationUi {
        val campaign = getCampaign(campaignId) ?: throw IllegalArgumentException("Campaign not found")

        // update campaign raised amount (UI-only)
        val idx = campaigns.indexOfFirst { it.id == campaignId }
        if (idx != -1) {
            val old = campaigns[idx]
            campaigns[idx] = old.copy(
                raisedAmount = old.raisedAmount + amount,
                lastUpdate = "Just now"
            )
        }

        val donation = DonationUi(
            id = UUID.randomUUID().toString(),
            campaignTitle = campaign.title,
            amount = amount,
            status = "Successful",
            date = "Today",
            receiptRef = "RCP-${(100..999).random()}"
        )
        donations.add(0, donation)
        return donation
    }
}