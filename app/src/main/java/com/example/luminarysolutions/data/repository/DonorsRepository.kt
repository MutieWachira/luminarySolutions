package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.Donor
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing donor data.
 * Acts as a clean data access layer for the ViewModel, ensuring separation of concerns.
 */
class DonorsRepository {

    /**
     * Returns a real-time stream of all donors.
     */
    fun getDonors(): Flow<List<Donor>> = FirestoreService.getDonors()

    /**
     * Returns a paginated stream of donors.
     */
    fun getDonorsPaginated(lastDocument: DocumentSnapshot?, pageSize: Long): Flow<Pair<List<Donor>, DocumentSnapshot?>> {
        return FirestoreService.getDonorsPaginated(lastDocument, pageSize)
    }

    /**
     * Adds a new donor to the system.
     */
    fun addDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        FirestoreService.addDonor(donor, onComplete)
    }

    /**
     * Updates an existing donor's information.
     */
    fun updateDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        FirestoreService.updateDonor(donor, onComplete)
    }

    /**
     * Deletes a donor from the system.
     */
    fun deleteDonor(donorId: String, onComplete: (Boolean) -> Unit) {
        FirestoreService.deleteDonor(donorId, onComplete)
    }
}
