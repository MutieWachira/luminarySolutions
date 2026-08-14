package com.example.luminarysolutions.data.repository

import android.util.Log
import com.example.luminarysolutions.data.models.Achievement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) {
    private val POINTS_PER_TASK = 100
    private val POINTS_PER_DONATION = 200
    private val POINTS_PER_LEVEL = 500

    private val DONOR_ACHIEVEMENTS = listOf(
        // --- VOLUME: The "Impact Streak" Pillar (10) ---
        Achievement("donor_v1", "First Spark", "Your first donation to a cause.", "bronze", 100, "DONATION_COUNT", 1, "DONOR"),
        Achievement("donor_v5", "Kind Heart", "5 donations reached.", "bronze", 250, "DONATION_COUNT", 5, "DONOR"),
        Achievement("donor_v10", "Regular Giver", "10 donations reached.", "silver", 500, "DONATION_COUNT", 10, "DONOR"),
        Achievement("donor_v20", "Pillar of Support", "20 donations reached.", "silver", 750, "DONATION_COUNT", 20, "DONOR"),
        Achievement("donor_v50", "Philanthropist", "50 donations reached.", "gold", 1500, "DONATION_COUNT", 50, "DONOR"),
        Achievement("donor_v75", "Change Maker", "75 donations reached.", "gold", 2000, "DONATION_COUNT", 75, "DONOR"),
        Achievement("donor_v100", "Century Giver", "100 donations reached.", "platinum", 5000, "DONATION_COUNT", 100, "DONOR"),
        Achievement("donor_v150", "Grand Patron", "150 donations reached.", "platinum", 7500, "DONATION_COUNT", 150, "DONOR"),
        Achievement("donor_v200", "Impact Legend", "200 donations reached.", "platinum", 10000, "DONATION_COUNT", 200, "DONOR"),
        Achievement("donor_v500", "Luminous Icon", "500 donations reached.", "platinum", 25000, "DONATION_COUNT", 500, "DONOR"),

        // --- FINANCIAL: The "Magnitude" Pillar (KES) (10) ---
        Achievement("amt_1k", "Supporter", "Contributed KES 1,000.", "bronze", 100, "TOTAL_DONATED", 1000, "DONOR"),
        Achievement("amt_5k", "Sustainer", "Contributed KES 5,000.", "bronze", 250, "TOTAL_DONATED", 5000, "DONOR"),
        Achievement("amt_10k", "Silver Donor", "Contributed KES 10,000.", "silver", 500, "TOTAL_DONATED", 10000, "DONOR"),
        Achievement("amt_25k", "Gold Donor", "Contributed KES 25,000.", "silver", 750, "TOTAL_DONATED", 25000, "DONOR"),
        Achievement("amt_50k", "Major Supporter", "Contributed KES 50,000.", "gold", 1500, "TOTAL_DONATED", 50000, "DONOR"),
        Achievement("amt_100k", "Impact Titan", "Contributed KES 100,000.", "gold", 3000, "TOTAL_DONATED", 100000, "DONOR"),
        Achievement("amt_250k", "Legacy Builder", "Contributed KES 250,000.", "platinum", 5000, "TOTAL_DONATED", 250000, "DONOR"),
        Achievement("amt_500k", "Luminary Hero", "Contributed KES 500,000.", "platinum", 10000, "TOTAL_DONATED", 500000, "DONOR"),
        Achievement("amt_1m", "Visionary Patron", "Contributed KES 1,000,000.", "platinum", 25000, "TOTAL_DONATED", 1000000, "DONOR"),
        Achievement("amt_5m", "Global Benefactor", "Contributed KES 5,000,000.", "platinum", 100000, "TOTAL_DONATED", 5000000, "DONOR"),

        // --- DIVERSITY: The "Sectors" Pillar (10) ---
        Achievement("div_cat1", "Specialist", "Supported 1 category.", "bronze", 100, "UNIQUE_CATEGORIES", 1, "DONOR"),
        Achievement("div_cat2", "Dual Supporter", "Supported 2 different sectors.", "bronze", 200, "UNIQUE_CATEGORIES", 2, "DONOR"),
        Achievement("div_cat3", "Cause Explorer", "Supported 3 different sectors.", "silver", 400, "UNIQUE_CATEGORIES", 3, "DONOR"),
        Achievement("div_cat4", "Broad Impact", "Supported 4 different sectors.", "silver", 600, "UNIQUE_CATEGORIES", 4, "DONOR"),
        Achievement("div_cat5", "Global Guardian", "Supported 5 different sectors.", "gold", 1000, "UNIQUE_CATEGORIES", 5, "DONOR"),
        Achievement("div_cat6", "Universal Donor", "Supported 6 different sectors.", "gold", 1500, "UNIQUE_CATEGORIES", 6, "DONOR"),
        Achievement("div_cat7", "Community Shield", "Supported 7 different sectors.", "platinum", 2000, "UNIQUE_CATEGORIES", 7, "DONOR"),
        Achievement("div_cat8", "World Protector", "Supported 8 different sectors.", "platinum", 3000, "UNIQUE_CATEGORIES", 8, "DONOR"),
        Achievement("div_cat9", "Impact Architect", "Supported 9 different sectors.", "platinum", 4000, "UNIQUE_CATEGORIES", 9, "DONOR"),
        Achievement("div_cat10", "Master of Change", "Supported all 10 focus areas.", "platinum", 10000, "UNIQUE_CATEGORIES", 10, "DONOR"),

        // --- ENGAGEMENT: The "Response" Pillar (10) ---
        Achievement("eng_urgent1", "First Responder", "Donated to 1 urgent campaign.", "bronze", 300, "URGENT_COUNT", 1, "DONOR"),
        Achievement("eng_urgent5", "Emergency Hero", "Donated to 5 urgent campaigns.", "silver", 1000, "URGENT_COUNT", 5, "DONOR"),
        Achievement("eng_holiday", "Season of Giving", "Donated during a holiday campaign.", "bronze", 200, "HOLIDAY_DONATION", 1, "DONOR"),
        Achievement("eng_profile", "Verified Impact", "Complete your donor profile.", "bronze", 100, "PROFILE_COMPLETE", 1, "DONOR"),
        Achievement("eng_recurring", "Clockwork Giver", "Set up a recurring donation.", "silver", 500, "RECURRING_SETUP", 1, "DONOR"),
        Achievement("eng_share", "Advocate", "Share a campaign 10 times.", "bronze", 200, "SHARE_COUNT", 10, "DONOR"),
        Achievement("eng_ref1", "Recruiter", "Refer 1 friend to Luminary.", "silver", 500, "REFERRAL_COUNT", 1, "DONOR"),
        Achievement("eng_ref5", "Networker", "Refer 5 friends to Luminary.", "gold", 2000, "REFERRAL_COUNT", 5, "DONOR"),
        Achievement("eng_top1", "Top 1%", "Be in the top 1% of donors this month.", "gold", 5000, "MONTHLY_RANK", 1, "DONOR"),
        Achievement("eng_early", "Early Bird", "Donate to a campaign in its first hour.", "silver", 1000, "EARLY_DONOR", 1, "DONOR"),

        // --- SPECIAL: The "Legacy" Pillar (10) ---
        Achievement("spec_1yr", "Anniversary", "Donor for 1 year.", "silver", 1000, "YEARS_ACTIVE", 1, "DONOR"),
        Achievement("spec_5yr", "Old Guard", "Donor for 5 years.", "gold", 5000, "YEARS_ACTIVE", 5, "DONOR"),
        Achievement("spec_all_cat", "Omnipresent", "Supported every category once.", "platinum", 15000, "ALL_CATEGORIES", 1, "DONOR"),
        Achievement("spec_major", "High Roller", "A single donation over KES 100,000.", "gold", 5000, "SINGLE_MAX", 100000, "DONOR"),
        Achievement("spec_night", "Night Owl", "Donate between 12 AM and 5 AM.", "bronze", 100, "TIME_DONATION", 0, "DONOR"),
        Achievement("spec_morning", "Early Riser", "Donate between 5 AM and 9 AM.", "bronze", 100, "TIME_DONATION", 1, "DONOR"),
        Achievement("spec_streak3", "Quarterly King", "Donate 3 months in a row.", "silver", 800, "STREAK_MONTHS", 3, "DONOR"),
        Achievement("spec_streak12", "Devoted", "Donate every month for a year.", "gold", 5000, "STREAK_MONTHS", 12, "DONOR"),
        Achievement("spec_anonymous", "Silent Hero", "Make an anonymous donation.", "bronze", 150, "ANONYMOUS", 1, "DONOR"),
        Achievement("spec_platinum_all", "Grand Master", "Unlock 40 other achievements.", "platinum", 25000, "ACHIEVEMENT_COUNT", 40, "DONOR")
    )

    private val VOLUNTEER_ACHIEVEMENTS = listOf(
        Achievement("task_1", "Quick Starter", "Complete your first task", "bronze", 50, "TASK_COUNT", 1, "VOLUNTEER"),
        Achievement("task_10", "Reliable Worker", "Complete 10 tasks", "silver", 150, "TASK_COUNT", 10, "VOLUNTEER"),
        Achievement("task_50", "The Half Century", "Complete 50 tasks", "gold", 500, "TASK_COUNT", 50, "VOLUNTEER"),
        Achievement("proj_1", "The Newcomer", "Join your first project", "bronze", 50, "PROJECT_COUNT", 1, "VOLUNTEER"),
        Achievement("lvl_5", "Rising Star", "Reach Level 5", "silver", 150, "LEVEL", 5, "VOLUNTEER")
    )

    private fun getAchievementsCollection() = db.collection("lumisphere").document("achievements").collection("items")
    private fun getDonorsCollection() = db.collection("lumisphere").document("donors").collection("items")
    private fun getVolunteersCollection() = db.collection("lumisphere").document("volunteers").collection("items")

    fun getAchievements(): Flow<List<Achievement>> = callbackFlow {
        val reg = getAchievementsCollection().addSnapshotListener { snp, _ ->
            trySend(snp?.documents?.mapNotNull { mapToAchievement(it) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun processGamification(userId: String, actionType: String, data: Map<String, Any> = emptyMap()): Result<Unit> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val role = userDoc.getString("role") ?: "DONOR"
            if (role == "DONOR") {
                processDonorAchievements(userId, data)
            } else {
                processVolunteerGamification(userId, 
                    taskCompleted = actionType == "TASK_COMPLETE",
                    projectJoined = actionType == "PROJECT_JOIN"
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processDonorAchievements(donorId: String, data: Map<String, Any>) {
        val donorRef = getDonorsCollection().document(donorId)
        db.runTransaction { transaction ->
            val doc = transaction.get(donorRef)
            if (!doc.exists()) return@runTransaction
            
            val donationAmount = (data["amount"] as? Number)?.toInt() ?: 0
            val category = data["category"] as? String ?: ""
            val isUrgent = data["isUrgent"] as? Boolean ?: false

            var newPoints = doc.getLong("points")?.toInt() ?: 0
            val unlockedIds = (doc.get("achievements") as? List<String>)?.toMutableList() ?: mutableListOf()
            
            val newTotalDonated = (doc.getLong("totalDonated") ?: 0) + donationAmount
            val newDonationCount = (doc.getLong("donationCount") ?: 0) + (if (donationAmount > 0) 1 else 0)
            
            val donatedCategories = (doc.get("donatedCategories") as? List<String>)?.toMutableSet() ?: mutableSetOf()
            if (category.isNotEmpty()) donatedCategories.add(category)

            if (donationAmount > 0) newPoints += POINTS_PER_DONATION

            DONOR_ACHIEVEMENTS.forEach { ach ->
                if (!unlockedIds.contains(ach.id)) {
                    val met = when (ach.criteriaType) {
                        "DONATION_COUNT" -> newDonationCount >= ach.criteriaValue
                        "TOTAL_DONATED" -> newTotalDonated >= ach.criteriaValue
                        "UNIQUE_CATEGORIES" -> donatedCategories.size >= ach.criteriaValue
                        "URGENT_COUNT" -> {
                            val urgentCount = (doc.getLong("urgentCount") ?: 0) + (if (isUrgent) 1 else 0)
                            urgentCount >= ach.criteriaValue
                        }
                        "SINGLE_MAX" -> donationAmount >= ach.criteriaValue
                        "ACHIEVEMENT_COUNT" -> unlockedIds.size >= ach.criteriaValue
                        "TIME_DONATION" -> {
                            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            when (ach.criteriaValue) {
                                0 -> hour in 0..5 // Night Owl
                                1 -> hour in 5..9 // Early Riser
                                else -> false
                            }
                        }
                        else -> false
                    }
                    if (met) {
                        unlockedIds.add(ach.id)
                        newPoints += ach.pointsAwarded
                        // In a real app, this should be an async call or triggered by a cloud function
                        Log.d("Gamification", "Trophy Unlocked: ${ach.title}")
                    }
                }
            }

            val newLevel = (newPoints / POINTS_PER_LEVEL) + 1
            transaction.update(donorRef, mapOf(
                "points" to newPoints,
                "level" to newLevel,
                "achievements" to unlockedIds,
                "totalDonated" to newTotalDonated,
                "donationCount" to newDonationCount,
                "donatedCategories" to donatedCategories.toList()
            ))
        }.await()
    }

    private suspend fun processVolunteerGamification(volunteerId: String, taskCompleted: Boolean = false, projectJoined: Boolean = false) {
        val volunteerRef = getVolunteersCollection().document(volunteerId)
        val doc = volunteerRef.get().await()
        if (!doc.exists()) return
        
        var newPoints = doc.getLong("points")?.toInt() ?: 0
        val unlockedIds = (doc.get("achievements") as? List<String>)?.toMutableList() ?: mutableListOf()
        val projectIds = (doc.get("projectIds") as? List<String>) ?: emptyList()
        val skills = (doc.get("skills") as? List<String>) ?: emptyList()
        val profileImageUrl = doc.getString("profileImageUrl")
        val motivation = doc.getString("motivation") ?: ""

        if (taskCompleted) newPoints += POINTS_PER_TASK
        
        val newLevel = (newPoints / POINTS_PER_LEVEL) + 1
        
        VOLUNTEER_ACHIEVEMENTS.forEach { ach ->
            if (!unlockedIds.contains(ach.id)) {
                val met = when (ach.criteriaType) {
                    "TASK_COUNT" -> (newPoints / POINTS_PER_TASK) >= ach.criteriaValue
                    "PROJECT_COUNT" -> projectIds.size >= ach.criteriaValue
                    "LEVEL" -> newLevel >= ach.criteriaValue
                    "SKILL_COUNT" -> skills.size >= ach.criteriaValue
                    "PROFILE_PIC" -> !profileImageUrl.isNullOrBlank()
                    "PROFILE_BIO" -> motivation.length > 20
                    "PLATINUM" -> unlockedIds.size >= ach.criteriaValue
                    else -> false
                }
                
                if (met) {
                    unlockedIds.add(ach.id)
                    newPoints += ach.pointsAwarded
                }
            }
        }
        
        val finalLevel = (newPoints / POINTS_PER_LEVEL) + 1
        volunteerRef.update(mapOf(
            "points" to newPoints,
            "level" to finalLevel,
            "achievements" to unlockedIds,
            "trophiesCount" to unlockedIds.size
        )).await()
    }

    private fun mapToAchievement(doc: com.google.firebase.firestore.DocumentSnapshot) = Achievement(
        id = doc.id,
        title = doc.getString("title") ?: "",
        description = doc.getString("description") ?: "",
        iconUrl = doc.getString("iconUrl"),
        pointsAwarded = doc.getLong("pointsAwarded")?.toInt() ?: 0,
        criteriaType = doc.getString("criteriaType") ?: "",
        criteriaValue = doc.getLong("criteriaValue")?.toInt() ?: 0,
        role = doc.getString("role")?.uppercase() ?: ""
    )

    private fun mapFromAchievement(a: Achievement) = hashMapOf(
        "title" to a.title,
        "description" to a.description,
        "pointsAwarded" to a.pointsAwarded,
        "criteriaType" to a.criteriaType,
        "criteriaValue" to a.criteriaValue,
        "iconUrl" to a.iconUrl,
        "role" to a.role
    )

    suspend fun seedAchievements() {
        try {
            val snp = getAchievementsCollection().get().await()
            val allDocs = snp.documents
            val ALL_ACHIEVEMENTS = DONOR_ACHIEVEMENTS + VOLUNTEER_ACHIEVEMENTS
            
            val needsSeeding = ALL_ACHIEVEMENTS.any { ach ->
                val existing = allDocs.find { it.id == ach.id }
                existing == null || existing.getString("role").isNullOrBlank()
            }

            if (needsSeeding) {
                val batch = db.batch()
                ALL_ACHIEVEMENTS.forEach { ach ->
                    batch.set(getAchievementsCollection().document(ach.id), mapFromAchievement(ach), SetOptions.merge())
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e("Gamification", "Seed failed", e)
        }
    }
}
