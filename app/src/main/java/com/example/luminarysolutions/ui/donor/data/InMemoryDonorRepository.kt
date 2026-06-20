package com.example.luminarysolutions.ui.donor.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class InMemoryDonorRepository : DonorRepository {

    private val campaigns = mutableStateListOf(
        CampaignUi(UUID.randomUUID().toString(), "Clean Water Initiative", "Water", "Kibera", 200000, 120000, "2 days ago", "https://images.unsplash.com/photo-1541252260730-0412e8e2108e?q=80&w=400"),
        CampaignUi(UUID.randomUUID().toString(), "Youth Skills Program", "Education", "Mathare", 150000, 65000, "Today", "https://images.unsplash.com/photo-1509062522246-3755977927d7?q=80&w=400"),
        CampaignUi(UUID.randomUUID().toString(), "Community Health Outreach", "Health", "Kayole", 300000, 210000, "Yesterday", "https://images.unsplash.com/photo-1576091160550-2173dad99901?q=80&w=400")
    )

    private val donations = mutableStateListOf(
        DonationUi(UUID.randomUUID().toString(), "Clean Water Initiative", 5000, "Successful", "Feb 20, 2026", "RCP-001"),
        DonationUi(UUID.randomUUID().toString(), "Youth Skills Program", 2000, "Successful", "Feb 22, 2026", "RCP-002")
    )

    private val reports = mutableStateListOf(
        ImpactReportUi(UUID.randomUUID().toString(), "Q1 Impact Report", "Water", "Jan–Mar", "Apr 05, 2026",
            "Highlights: water access improved, skills training delivered, community outreach expanded."),
        ImpactReportUi(UUID.randomUUID().toString(), "Mid-Year Transparency Brief", "Education", "Jan–Jun", "Jul 10, 2026",
            "Summary: spending categories, milestone progress, and community feedback trends.")
    )

    override fun getCampaignsFlow(category: String?): Flow<List<CampaignUi>> = snapshotFlow {
        if (category == null || category == "All") campaigns.toList()
        else campaigns.filter { it.category == category }
    }

    override fun getCampaign(campaignId: String): Flow<CampaignUi?> = snapshotFlow {
        campaigns.firstOrNull { it.id == campaignId }
    }

    override fun getMyDonations(userId: String): Flow<List<DonationUi>> = snapshotFlow {
        donations.toList()
    }

    override fun getReports(category: String?): Flow<List<ImpactReportUi>> = snapshotFlow {
        if (category == null || category == "All") reports.toList()
        else reports.filter { it.category == category }
    }

    override suspend fun donate(userId: String, campaignId: String, amount: Int): Result<Unit> {
        val campaign = campaigns.firstOrNull { it.id == campaignId } 
            ?: return Result.failure(IllegalArgumentException("Campaign not found"))

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
        return Result.success(Unit)
    }

    override suspend fun volunteerSignup(userId: String, campaignId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun isAlreadyVolunteering(userId: String): Boolean = false

    override suspend fun isVolunteeringForCampaign(userId: String, campaignId: String): Boolean = false

    override suspend fun isVolunteer(userId: String): Boolean = false

    override suspend fun joinProject(userId: String, campaignId: String): Result<Unit> = Result.success(Unit)
}