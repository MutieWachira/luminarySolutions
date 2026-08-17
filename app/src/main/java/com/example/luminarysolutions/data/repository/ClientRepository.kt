package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.DashboardStats
import com.example.luminarysolutions.data.models.Enquiry
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.ServiceAcquisition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClientRepository: Handles data operations for the Client module.
 * Focused on freelance services and client-specific interactions.
 */
@Singleton
class ClientRepository @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    
    /**
     * Fetches all available freelance services offered by Luminary.
     */
    fun getFreelanceServices(): Flow<List<Freelance>> = projectRepository.getLuminaryProjects()

    /**
     * Fetches projects associated with a specific client.
     */
    fun getClientProjects(clientId: String): Flow<List<Freelance>> = 
        projectRepository.getLuminaryProjects().map { projects ->
            projects.filter { it.clientIds.contains(clientId) }
        }

    /**
     * Calculates dashboard statistics for a specific client.
     */
    fun getDashboardStats(clientId: String): Flow<DashboardStats> = 
        getClientProjects(clientId).map { projects ->
            DashboardStats(
                activeProjectsCount = projects.count { it.status == "In Progress" },
                pendingRequestsCount = projects.count { it.status == "Pending" },
                completedProjectsCount = projects.count { it.status == "Completed" },
                totalInvested = 0.0 // Placeholder for future finance integration
            )
        }

    /**
     * Fetches a specific freelance service by its ID.
     */
    fun getServiceById(serviceId: String): Flow<Freelance?> = 
        projectRepository.getLuminaryProjects().map { it.find { p -> p.id == serviceId } }

    /**
     * Allows a client to apply for a freelance service.
     */
    suspend fun applyForService(serviceId: String, clientId: String): Result<Unit> {
        // Placeholder for actual implementation - e.g., adding to application collection
        return Result.success(Unit)
    }

    /**
     * Sends an enquiry regarding a service.
     */
    suspend fun sendEnquiry(enquiry: Enquiry): Result<Unit> {
        return projectRepository.sendEnquiry(enquiry)
    }

    /**
     * Acquires a service.
     */
    suspend fun acquireService(acquisition: ServiceAcquisition): Result<Unit> {
        return projectRepository.acquireService(acquisition)
    }
}
