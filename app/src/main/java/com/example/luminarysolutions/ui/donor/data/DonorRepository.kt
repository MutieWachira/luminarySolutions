package com.example.luminarysolutions.ui.donor.data

import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import kotlinx.coroutines.flow.Flow

/**
 * Clean Architecture Repository for Donor operations.
 * Handles Campaigns, Donations, and Impact Reports with real-time support.
 */
interface DonorRepository {
    /**
     * Returns a real-time stream of campaigns.
     */
    fun getCampaignsFlow(category: String? = null): Flow<List<CampaignUi>>
    
    /**
     * Returns a single campaign by ID.
     */
    fun getCampaign(campaignId: String): Flow<CampaignUi?>
    
    /**
     * Returns donation history for a specific user.
     */
    fun getMyDonations(userId: String): Flow<List<DonationUi>>
    
    /**
     * Returns impact reports.
     */
    fun getReports(category: String? = null): Flow<List<ImpactReportUi>>

    /**
     * Records a donation.
     */
    suspend fun donate(userId: String, campaignId: String, amount: Int): Result<Unit>
    
    /**
     * Signs up a volunteer for a specific campaign.
     * Constraint: Only one active program (campaign) signup allowed at a time.
     */
    suspend fun volunteerSignup(userId: String, campaignId: String): Result<Unit>
    
    /**
     * Checks if the user is already signed up for ANY campaign.
     */
    suspend fun isAlreadyVolunteering(userId: String): Boolean

    /**
     * Checks if the user is signed up for a SPECIFIC campaign.
     */
    suspend fun isVolunteeringForCampaign(userId: String, campaignId: String): Boolean

    /**
     * Checks if the user has a volunteer role.
     */
    suspend fun isVolunteer(userId: String): Boolean

    /**
     * Joins a project for an existing volunteer.
     */
    suspend fun joinProject(userId: String, campaignId: String): Result<Unit>
}
