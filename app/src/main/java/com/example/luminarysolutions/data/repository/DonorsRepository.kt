package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.User
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing donor data.
 * Acts as a clean data access layer for the ViewModel, ensuring separation of concerns.
 */
@Singleton
class DonorsRepository @Inject constructor(
    private val financeRepository: FinanceRepository
) {

    /**
     * Returns a real-time stream of all donors.
     */
    fun getDonors(): Flow<List<Donor>> = financeRepository.getDonors()

    /**
     * Returns a paginated stream of donors.
     */
    fun getDonorsPaginated(lastDocument: DocumentSnapshot?, pageSize: Long): Flow<Pair<List<Donor>, DocumentSnapshot?>> {
        return financeRepository.getDonorsPaginated(lastDocument, pageSize)
    }

    /**
     * Adds a new donor to the system.
     */
    suspend fun addDonor(donor: Donor): Result<Unit> = financeRepository.addDonor(donor)

    /**
     * Updates an existing donor's information.
     */
    suspend fun updateDonor(donor: Donor): Result<Unit> = financeRepository.updateDonor(donor)

    /**
     * Deletes a donor from the system.
     */
    suspend fun deleteDonor(donorId: String): Result<Unit> = financeRepository.deleteDonor(donorId)

    /**
     * Registers a new donor in the system with full user profile.
     */
    suspend fun registerDonor(user: User, donor: Donor): Result<Unit> = financeRepository.registerDonor(user, donor)
}
