package com.example.luminarysolutions.ui.donor.data

import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.DonationUi
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.luminarysolutions.data.firebase.FirestoreService
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.text.SimpleDateFormat
import java.util.*

/**
 * Production implementation of DonorRepository using Firebase Firestore.
 * Ensures real-time updates and proper data mapping.
 */
class DonorRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DonorRepository {
    private val projectsCollection = db.collection("lumisphere").document("projects").collection("items")
    private val volunteersCollection = db.collection("lumisphere").document("volunteers").collection("items")
    private val donationsCollection = db.collection("lumisphere").document("donations").collection("items")
    private val reportsCollection = db.collection("lumisphere").document("reports").collection("items")

    override fun getCampaignsFlow(category: String?): Flow<List<CampaignUi>> = callbackFlow {
        // Industry Best Practice: Filter by category if provided.
        // To avoid FAILED_PRECONDITION (missing index) errors when filtering AND ordering,
        // we'll handle sorting client-side for better robustness across different environments.
        var query: Query = projectsCollection
        
        if (category != null && category != "All") {
            query = query.whereEqualTo("category", category)
        }
        
        // We removed Firestore-level orderBy to prevent crashes if composite indexes are missing.
        // Instead, we'll sort the resulting list in memory.

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val campaigns = snapshot?.documents?.mapNotNull { doc ->
                try {
                    CampaignUi(
                        id = doc.id,
                        title = doc.getString("name") ?: "Unnamed Campaign",
                        category = doc.getString("category") ?: "General",
                        location = doc.getString("location") ?: "Unknown",
                        goalAmount = doc.getLong("budget")?.toInt() ?: 0,
                        raisedAmount = doc.getLong("spent")?.toInt() ?: 0,
                        lastUpdate = "Updated recently",
                        imageUrl = doc.getString("imageUrl")
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            
            // Client-side sort by raised amount or title as a fallback for 'lastUpdated'
            trySend(campaigns.sortedByDescending { it.raisedAmount })
        }
        awaitClose { listener.remove() }
    }

    override fun getCampaign(campaignId: String): Flow<CampaignUi?> = callbackFlow {
        val listener = projectsCollection.document(campaignId).addSnapshotListener { doc, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                trySend(
                    CampaignUi(
                        id = doc.id,
                        title = doc.getString("name") ?: "Unnamed Campaign",
                        category = doc.getString("category") ?: "General",
                        location = doc.getString("location") ?: "Unknown",
                        goalAmount = doc.getLong("budget")?.toInt() ?: 0,
                        raisedAmount = doc.getLong("spent")?.toInt() ?: 0,
                        lastUpdate = "Updated recently",
                        imageUrl = doc.getString("imageUrl")
                    )
                )
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getMyDonations(userId: String): Flow<List<DonationUi>> = callbackFlow {
        // Robust query for donations: removed orderBy to avoid index requirement.
        val query = donationsCollection.whereEqualTo("donorId", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val donations = snapshot?.documents?.mapNotNull { doc ->
                    DonationUi(
                        id = doc.id,
                        campaignTitle = doc.getString("campaignTitle") ?: "Donation",
                        amount = doc.getLong("amount")?.toInt() ?: 0,
                        status = doc.getString("status") ?: "Successful",
                        date = doc.getString("date") ?: "Recently",
                        receiptRef = doc.getString("receiptNo") ?: "RCP-000"
                    )
                } ?: emptyList()
                trySend(donations)
            }
        awaitClose { listener.remove() }
    }

    override fun getReports(category: String?): Flow<List<ImpactReportUi>> = callbackFlow {
        var query: Query = reportsCollection
        
        if (category != null && category != "All") {
            query = query.whereEqualTo("category", category)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    ImpactReportUi(
                        id = doc.id,
                        title = doc.getString("title") ?: "Impact Report",
                        category = doc.getString("category") ?: "General",
                        period = doc.getString("period") ?: "Quarterly",
                        publishedOn = doc.getString("date") ?: "Today",
                        summary = doc.getString("summary") ?: ""
                    )
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun donate(userId: String, campaignId: String, amount: Int): Result<Unit> = suspendCancellableCoroutine { continuation ->
        FirestoreService.addDonation(
            donorId = userId,
            amount = amount,
            method = "In-App",
            projectId = campaignId,
            details = mapOf("source" to "DonorApp"),
            onComplete = { success ->
                if (success) continuation.resume(Result.success(Unit))
                else continuation.resume(Result.failure(Exception("Donation transaction failed")))
            }
        )
    }

    override suspend fun volunteerSignup(userId: String, campaignId: String): Result<Unit> = try {
        db.runTransaction { transaction ->
            val projectRef = projectsCollection.document(campaignId)
            
            // 1. Add user to project's volunteer list
            transaction.update(projectRef, "volunteers", FieldValue.arrayUnion(userId))
            
            // 2. Update Volunteer collection list
            val volunteerRef = volunteersCollection.document(userId)
            transaction.update(volunteerRef, "projectIds", FieldValue.arrayUnion(campaignId))

            // 3. Track active program signup (optional: can store multiple or just latest)
            val activeProgramRef = db.collection("users").document(userId).collection("active_programs").document(campaignId)
            transaction.set(activeProgramRef, mapOf(
                "campaignId" to campaignId,
                "signupDate" to FieldValue.serverTimestamp()
            ))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        // Fallback for document not existing yet or other errors
        try {
            volunteersCollection.document(userId).update("projectIds", FieldValue.arrayUnion(campaignId)).await()
            Result.success(Unit)
        } catch (e2: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAlreadyVolunteering(userId: String): Boolean {
        val doc = db.collection("lumisphere").document("volunteers").collection("items").document(userId).get().await()
        val projects = doc.get("projectIds") as? List<*>
        return projects != null && projects.isNotEmpty()
    }

    override suspend fun isVolunteeringForCampaign(userId: String, campaignId: String): Boolean {
        val doc = db.collection("lumisphere").document("volunteers").collection("items").document(userId).get().await()
        val projects = doc.get("projectIds") as? List<*>
        return projects?.contains(campaignId) == true
    }

    override suspend fun isVolunteer(userId: String): Boolean {
        val doc = db.collection("users").document(userId).get().await()
        return doc.getString("role") == "VOLUNTEER"
    }

    override suspend fun joinProject(userId: String, campaignId: String): Result<Unit> = volunteerSignup(userId, campaignId)
}
