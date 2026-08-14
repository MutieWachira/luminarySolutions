package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Freelance
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
}
