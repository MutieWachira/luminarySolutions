package com.example.luminarysolutions.data.repository


import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Partner
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing partnership data
 * Acts as a clean data access layer for the Viewmodel
 */

class PartnersRepository {
    /**
     * Returns a real-time stream of partners from the data source
     */
    fun getPartners(): Flow<List<Partner>> {
        return FirestoreService.getPartners()
    }

    /**
     * Adds a new partner to the system
     * @param partner The partner data to save
     * @param onComplete Callback to notify the caller of success or failure
     */

    fun addPartner(partner: Partner, onComplete: (Boolean) -> Unit){
        FirestoreService.addPartner(partner, onComplete)
    }
}