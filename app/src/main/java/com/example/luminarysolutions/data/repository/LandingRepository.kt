package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.ui.donor.models.CampaignUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for landing page data.
 * Provides public metrics and campaigns for unauthenticated users.
 */
class LandingRepository {
    
    /**
     * Fetches public campaigns.
     * In a production app, this might use a specific 'public' collection or filter.
     */
    fun getPublicCampaigns(): Flow<List<CampaignUi>> {
        // Fetching from FirestoreService projects and mapping to CampaignUi
        return FirestoreService.getProjects().map { projects ->
            projects.map { 
                CampaignUi(
                    id = it.id,
                    title = it.name,
                    category = it.category,
                    location = it.location,
                    goalAmount = it.budget,
                    raisedAmount = it.spent,
                    lastUpdate = it.lastUpdated
                )
            }
        }
    }

    /**
     * Gets aggregate stats for the public dashboard.
     */
    fun getPublicStats(): Flow<PublicStats> {
        return FirestoreService.getProjects().map { projects ->
            PublicStats(
                totalRaised = projects.sumOf { it.spent },
                activeCampaigns = projects.size,
                impactReached = 15000 // Mocked value for demonstration
            )
        }
    }
}

data class PublicStats(
    val totalRaised: Int = 0,
    val activeCampaigns: Int = 0,
    val impactReached: Int = 0
)
