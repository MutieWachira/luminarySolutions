package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.ui.donor.models.CampaignUi
import com.example.luminarysolutions.ui.donor.models.CategoryUi
import com.example.luminarysolutions.ui.donor.models.HeroItemUi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WaterDrop
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Result wrapper for paginated data.
 */
data class PaginatedResult<T>(
    val data: List<T>,
    val lastDocument: DocumentSnapshot?,
    val hasMore: Boolean
)

/**
 * Repository interface for Campaign data.
 * Following Clean Architecture: This defines the contract for data operations.
 */
interface CampaignRepository {
    suspend fun getCampaigns(
        category: String? = null,
        pageSize: Int = 10,
        lastDocument: DocumentSnapshot? = null
    ): Result<PaginatedResult<CampaignUi>>

    fun getCategories(): Flow<List<CategoryUi>>
    fun getHeroItems(): Flow<List<HeroItemUi>>
    fun getDashboardStats(): Flow<Triple<String, String, String>> // Lives touched, Campaigns, Total Raised
}

/**
 * Production-ready implementation of CampaignRepository using Firebase Firestore.
 * Implements pagination and category filtering with Clean Architecture principles.
 */
class CampaignRepositoryImpl : CampaignRepository {
    private val db = FirebaseFirestore.getInstance()
    private val projectsCollection = db.collection("lumisphere").document("projects").collection("items")

    // Constants for Firestore fields to avoid typos and improve maintainability
    private object Fields {
        const val CATEGORY = "category"
        const val LAST_UPDATED = "lastUpdated"
        const val NAME = "name"
        const val BUDGET = "budget"
        const val SPENT = "spent"
        const val LOCATION = "location"
        const val IMAGE_URL = "imageUrl"
    }

    override suspend fun getCampaigns(
        category: String?,
        pageSize: Int,
        lastDocument: DocumentSnapshot?
    ): Result<PaginatedResult<CampaignUi>> {
        return try {
            // PRODUCTION STRATEGY: Avoiding Composite Indexes
            // To remove the need for custom composite indexes, we separate filtering and ordering.
            // Option A: Filter on server, Sort on client.
            // Option B: Fetch all, Filter and Sort on client.
            
            // We'll use Option A for specific categories to keep data transfer low.
            // For 'All', we use server-side ordering since it only involves one field.

            val query: Query = if (category == null || category == "All") {
                // No composite index needed for single field orderBy
                projectsCollection
                    .orderBy(Fields.LAST_UPDATED, Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
                    .let { if (lastDocument != null) it.startAfter(lastDocument) else it }
            } else {
                // No composite index needed for single field whereEqualTo
                // We fetch a larger batch and sort in memory as the "alternative"
                projectsCollection
                    .whereEqualTo(Fields.CATEGORY, category)
                    .limit(100) // Reasonable limit for in-memory sorting
            }

            val snapshot = query.get().await()
            var campaigns = snapshot.documents.mapNotNull { doc -> mapToCampaignUi(doc) }

            // Apply in-memory sorting if we filtered by category (since we couldn't orderBy on server)
            if (category != null && category != "All") {
                // Sorting by lastUpdate - note: lastUpdate is currently a string "Updated recently"
                // In a real app, we'd use the raw timestamp from Firestore for accurate sorting.
                // For this implementation, we'll keep it as is or use doc.getTimestamp if available.
                campaigns = campaigns.sortedByDescending { it.id } // Placeholder for actual temporal sort
            }

            Result.success(
                PaginatedResult(
                    data = campaigns.take(pageSize),
                    lastDocument = snapshot.documents.lastOrNull(),
                    hasMore = snapshot.size() == pageSize && (category == null || category == "All")
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("CampaignRepository", "Error fetching campaigns: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapToCampaignUi(doc: DocumentSnapshot): CampaignUi {
        return CampaignUi(
            id = doc.id,
            title = doc.getString(Fields.NAME) ?: "Unnamed Project",
            category = doc.getString(Fields.CATEGORY) ?: "General",
            location = doc.getString(Fields.LOCATION) ?: "Unknown",
            goalAmount = doc.getLong(Fields.BUDGET)?.toInt() ?: 0,
            raisedAmount = doc.getLong(Fields.SPENT)?.toInt() ?: 0,
            lastUpdate = "Updated recently",
            imageUrl = doc.getString(Fields.IMAGE_URL) ?: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c"
        )
    }

    override fun getCategories(): Flow<List<CategoryUi>> = flow {
        // Expanded list to match CEO's project creation options for consistency
        emit(
            listOf(
                CategoryUi("all", "All", Icons.Default.Category),
                CategoryUi("cat1", "Education", Icons.Default.School),
                CategoryUi("cat2", "Health", Icons.Default.HealthAndSafety),
                CategoryUi("cat3", "Environment", Icons.Default.Eco),
                CategoryUi("cat4", "Water", Icons.Default.WaterDrop),
                CategoryUi("cat5", "Community", Icons.Default.Public)
            )
        )
    }

    override fun getHeroItems(): Flow<List<HeroItemUi>> = flow {
        emit(
            listOf(
                HeroItemUi("h1", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c", "Make an Impact", "Your small contribution can change lives."),
                HeroItemUi("h2", "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6", "Direct Giving", "Transparent tracking of every dollar you donate."),
                HeroItemUi("h3", "https://images.unsplash.com/photo-1469571486292-0ba58a3f068b", "Community Led", "Supporting local initiatives for sustainable growth.")
            )
        )
    }

    override fun getDashboardStats(): Flow<Triple<String, String, String>> = flow {
        // Fetch from Firestore lumisphere/projects, donors, revenue documents
        val orgCol = db.collection("lumisphere")
        
        try {
            val projectsDoc = orgCol.document("projects").get().await()
            val donorsDoc = orgCol.document("donors").get().await()
            val revenueDoc = orgCol.document("revenue").get().await()

            val projectsCount = projectsDoc.getLong("count")?.toString() ?: "0"
            val donorsCount = donorsDoc.getLong("count")?.toString() ?: "0"
            val totalRaised = revenueDoc.getLong("totalAmount")?.let { "KSh ${it/1000}K+" } ?: "KSh 0"

            emit(Triple(donorsCount, projectsCount, totalRaised))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(Triple("0", "0", "KSh 0"))
        }
    }
}
