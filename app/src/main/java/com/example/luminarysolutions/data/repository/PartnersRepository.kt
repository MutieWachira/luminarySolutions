package com.example.luminarysolutions.data.repository


import com.example.luminarysolutions.data.models.Partner
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing partnership data
 * Acts as a clean data access layer for the Viewmodel
 */

@Singleton
class PartnersRepository @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    /**
     * Returns a real-time stream of partners from the data source
     */
    fun getPartners(): Flow<List<Partner>> = projectRepository.getPartners()

    /**
     * Adds a new partner to the system
     * @param partner The partner data to save
     */
    suspend fun addPartner(partner: Partner): Result<Unit> = projectRepository.addPartner(partner)
}