package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.ui.donor.models.CampaignUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for landing page data.
 * Provides public metrics and campaigns for unauthenticated users.
 */
@Singleton
class LandingRepository @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    
    /**
     * Fetches public campaigns.
     */
    fun getPublicCampaigns(): Flow<List<CampaignUi>> {
        return projectRepository.getProjects().map { projectsList ->
            projectsList.map { project ->
                CampaignUi(
                    id = project.id,
                    title = project.name,
                    category = project.category,
                    location = project.location,
                    goalAmount = project.budget,
                    raisedAmount = project.spent,
                    lastUpdate = project.lastUpdated
                )
            }
        }
    }

    /**
     * Gets aggregate stats for the public dashboard.
     */
    fun getPublicStats(): Flow<PublicStats> {
        return projectRepository.getProjects().map { projectsList ->
            PublicStats(
                totalRaised = projectsList.sumOf { it.spent },
                activeCampaigns = projectsList.size,
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
