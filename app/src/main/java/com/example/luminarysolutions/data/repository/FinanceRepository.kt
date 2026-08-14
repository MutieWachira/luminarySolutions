package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.firebase.LumOverviewDashboardStats
import com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats
import com.example.luminarysolutions.data.firebase.MonthlyFinancialStats
import com.example.luminarysolutions.data.firebase.MonthlyImpactStats
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.Expense
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    private val monthOrder = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

    private fun getDonorsCollection() = db.collection("lumisphere").document("donors").collection("items")
    private fun getExpensesCollection() = db.collection("lumisphere").document("expenses").collection("items")
    private fun getApprovalsCollection() = db.collection("lumisphere").document("approvals").collection("items")

    fun getLumDashStats(year: Int): Flow<LumOverviewDashboardStats> = callbackFlow {
        val orgCol = db.collection("luminary")
        var projectsCount = 0
        var totalExpenses = 0
        var totalRevenue = 0
        var activeClientCount = 0
        var monthlyStats = emptyList<MonthlyFinancialStats>()

        val emit = {
            val displayRevenue = if (totalRevenue == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.revenue } else totalRevenue
            val displayExpenses = if (totalExpenses == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.expenses } else totalExpenses
            trySend(LumOverviewDashboardStats(
                totalRevenue = displayRevenue,
                totalExpenses = displayExpenses,
                totalProfit = displayRevenue - displayExpenses,
                totalProjects = projectsCount,
                totalActiveClient = activeClientCount,
                monthlyStats = monthlyStats
            ))
        }

        val pListener = orgCol.document("freelances").addSnapshotListener { doc, _ -> projectsCount = (doc?.get("count") as? Number)?.toInt() ?: 0; emit() }
        val rListener = orgCol.document("revenue").addSnapshotListener { doc, _ -> totalRevenue = (doc?.get("revenue") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0; emit() }
        val eListener = orgCol.document("expenses").addSnapshotListener { doc, _ -> totalExpenses = (doc?.get("expense") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0; emit() }
        val cListener = orgCol.document("clients").addSnapshotListener { doc, _ -> activeClientCount = (doc?.get("count") as? Number)?.toInt() ?: 0; emit() }

        val mListener = orgCol.document("financials").collection("years").document(year.toString()).collection("months")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    monthlyStats = snapshot.documents.mapNotNull { doc ->
                        MonthlyFinancialStats(
                            month = doc.id.lowercase(),
                            revenue = (doc.get("revenue") as? Number)?.toInt() ?: 0,
                            expenses = (doc.get("expense") as? Number)?.toInt() ?: 0
                        )
                    }.sortedBy { monthOrder.indexOf(it.month) }
                    emit()
                }
            }

        awaitClose { pListener.remove(); rListener.remove(); eListener.remove(); cListener.remove(); mListener.remove() }
    }

    fun getLumiSphereDashStats(year: Int): Flow<LumiSphereOverviewDashboardStats> = callbackFlow {
        val orgCol = db.collection("lumisphere")
        var programsCount = 0; var totalDonations = 0; var totalSpent = 0; var totalBeneficiaries = 0; var monthlyStats = emptyList<MonthlyImpactStats>()

        val emit = {
            trySend(LumiSphereOverviewDashboardStats(
                totalDonations = totalDonations, totalSpent = totalSpent, totalPrograms = programsCount,
                totalBeneficiaries = totalBeneficiaries, impactScore = if (totalBeneficiaries > 0) 8.9f else 0f, monthlyStats = monthlyStats
            ))
        }

        val pListener = orgCol.document("projects").addSnapshotListener { doc, _ -> programsCount = (doc?.get("count") as? Number)?.toInt() ?: 0; emit() }
        val dListener = orgCol.document("donations").addSnapshotListener { doc, _ -> totalDonations = (doc?.get("totalAmount") as? Number)?.toInt() ?: 0; emit() }
        val eListener = orgCol.document("expenses").addSnapshotListener { doc, _ -> totalSpent = (doc?.get("totalAmount") as? Number)?.toInt() ?: 0; emit() }
        val bListener = orgCol.document("beneficiaries").addSnapshotListener { doc, _ -> totalBeneficiaries = (doc?.get("count") as? Number)?.toInt() ?: 0; emit() }
        val mListener = orgCol.document("impact").collection("years").document(year.toString()).collection("months")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    monthlyStats = snapshot.documents.mapNotNull { doc ->
                        MonthlyImpactStats(
                            month = doc.id.lowercase(),
                            donations = (doc.get("donations") as? Number)?.toInt() ?: 0,
                            beneficiaries = (doc.get("beneficiaries") as? Number)?.toInt() ?: 0
                        )
                    }.sortedBy { monthOrder.indexOf(it.month) }
                    emit()
                }
            }

        awaitClose { pListener.remove(); dListener.remove(); eListener.remove(); bListener.remove(); mListener.remove() }
    }

    fun getDashboardStats(): Flow<DashboardStats> = callbackFlow {
        val orgCol = db.collection("lumisphere")
        var pc = 0; var dc = 0; var pac = 0; var et = 0; var rt = 0
        val emit = { trySend(DashboardStats(pc, dc, et, pac, rt)) }

        val p = orgCol.document("projects").addSnapshotListener { d, _ -> pc = d?.getLong("count")?.toInt() ?: 0; emit() }
        val d = orgCol.document("donors").addSnapshotListener { doc, _ -> dc = doc?.getLong("count")?.toInt() ?: 0; emit() }
        val pa = orgCol.document("partners").addSnapshotListener { doc, _ -> pac = doc?.getLong("count")?.toInt() ?: 0; emit() }
        val e = orgCol.document("expenses").addSnapshotListener { doc, _ -> et = doc?.getLong("totalAmount")?.toInt() ?: 0; emit() }
        val r = orgCol.document("revenue").addSnapshotListener { doc, _ -> rt = doc?.getLong("totalAmount")?.toInt() ?: 0; emit() }

        awaitClose { p.remove(); d.remove(); pa.remove(); e.remove(); r.remove() }
    }

    fun getDonors(): Flow<List<Donor>> = callbackFlow {
        val reg = getDonorsCollection().orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToDonor(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    suspend fun addDonor(donor: Donor): Result<Unit> {
        return try {
            getDonorsCollection().add(mapFromDonor(donor)).await()
            db.collection("lumisphere").document("donors").update("count", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToDonor(doc: com.google.firebase.firestore.DocumentSnapshot) = Donor(
        id = doc.id,
        name = doc.getString("name") ?: "Unnamed",
        type = doc.getString("type") ?: "Donor",
        status = doc.getString("status") ?: "Active",
        valueOrNote = doc.getString("valueOrNote") ?: "—",
        lastContact = "Last contact: ${doc.get("lastContactDate")?.let { if (it is Timestamp) dateFormatter.format(it.toDate()) else it.toString() } ?: "No contact"}",
        points = doc.getLong("points")?.toInt() ?: 0,
        level = doc.getLong("level")?.toInt() ?: 1,
        achievements = doc.get("achievements") as? List<String> ?: emptyList(),
        totalDonated = doc.getLong("totalDonated")?.toInt() ?: 0,
        donationCount = doc.getLong("donationCount")?.toInt() ?: 0
    )

    private fun mapFromDonor(d: Donor) = hashMapOf(
        "name" to d.name,
        "type" to d.type,
        "status" to d.status,
        "valueOrNote" to d.valueOrNote,
        "lastContactDate" to FieldValue.serverTimestamp(),
        "points" to d.points,
        "level" to d.level,
        "achievements" to d.achievements,
        "totalDonated" to d.totalDonated,
        "donationCount" to d.donationCount
    )

    fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val reg = getExpensesCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ ->
                trySend(snp?.documents?.mapNotNull { mapToExpense(it) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun addExpense(expense: Expense): Result<Unit> {
        return try {
            getExpensesCollection().add(mapFromExpense(expense)).await()
            db.collection("lumisphere").document("expenses").update(
                "totalAmount", FieldValue.increment(expense.amount.toLong())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDonorById(donorId: String): Flow<Donor?> = callbackFlow {
        val registration = getDonorsCollection().document(donorId)
            .addSnapshotListener { doc, _ -> trySend(doc?.let { if (it.exists()) mapToDonor(it) else null }) }
        awaitClose { registration.remove() }
    }

    fun getDonorsPaginated(lastDocument: com.google.firebase.firestore.DocumentSnapshot?, pageSize: Long): Flow<Pair<List<Donor>, com.google.firebase.firestore.DocumentSnapshot?>> = callbackFlow {
        var query = getDonorsCollection().orderBy("lastContactDate", Query.Direction.DESCENDING).limit(pageSize)
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Pair(emptyList(), null))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val donors = snapshot.documents.mapNotNull { mapToDonor(it) }
                val lastDoc = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                trySend(Pair(donors, lastDoc))
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateDonor(donor: Donor): Result<Unit> {
        return try {
            if (donor.id.isEmpty()) return Result.failure(Exception("Donor ID is empty"))
            getDonorsCollection().document(donor.id).update(mapFromDonor(donor) as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDonor(donorId: String): Result<Unit> {
        return try {
            getDonorsCollection().document(donorId).delete().await()
            db.collection("lumisphere").document("donors").update("count", FieldValue.increment(-1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerDonor(user: com.example.luminarysolutions.data.models.User, donor: Donor): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.set(db.collection("users").document(user.id), mapFromUser(user))
            batch.set(getDonorsCollection().document(user.id), mapFromDonor(donor))
            batch.set(db.collection("lumisphere").document("donors"), mapOf("count" to FieldValue.increment(1)), SetOptions.merge())
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapFromUser(u: com.example.luminarysolutions.data.models.User) = hashMapOf(
        "name" to u.name,
        "email" to u.email,
        "phoneNumber" to u.phoneNumber,
        "bio" to u.bio,
        "profileImageUrl" to u.profileImageUrl,
        "role" to u.role.name,
        "enabled" to u.enabled,
        "fcmToken" to u.fcmToken,
        "isTwoFactorEnabled" to u.isTwoFactorEnabled,
        "darkModeEnabled" to u.darkModeEnabled,
        "notificationsEnabled" to u.notificationsEnabled
    )

    fun getApprovals(): Flow<List<Approval>> = callbackFlow {
        val reg = getApprovalsCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToApproval(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    suspend fun addApproval(approval: Approval): Result<Unit> {
        return try {
            getApprovalsCollection().add(mapFromApproval(approval)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapFromApproval(a: Approval) = hashMapOf(
        "type" to a.type,
        "account" to a.account,
        "amount" to a.amount,
        "priority" to a.priority,
        "status" to a.status,
        "requestedBy" to a.requestedBy,
        "timestamp" to FieldValue.serverTimestamp()
    )

    suspend fun addDonation(
        donorId: String?,
        amount: Double,
        method: String,
        projectId: String? = null,
        details: Map<String, String> = emptyMap()
    ): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val donationRef = db.collection("lumisphere").document("donations").collection("items").document()
                val summaryRef = db.collection("lumisphere").document("donations")
                val receiptNo = "LUM-${System.currentTimeMillis().toString().takeLast(6)}-${(10..99).random()}"
                
                val donationData = hashMapOf<String, Any?>(
                    "donorId" to donorId,
                    "projectId" to projectId,
                    "amount" to amount,
                    "method" to method,
                    "details" to details,
                    "status" to "Successful",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "receiptNo" to receiptNo
                )
                
                if (projectId != null) {
                    val projectSnap = transaction.get(db.collection("lumisphere").document("projects").collection("items").document(projectId))
                    if (projectSnap.exists()) {
                        donationData["campaignTitle"] = projectSnap.getString("name") ?: "Campaign"
                    }
                }

                if (projectId != null) {
                    val projectRef = db.collection("lumisphere").document("projects").collection("items").document(projectId)
                    transaction.update(projectRef, "spent", FieldValue.increment(amount))
                    transaction.update(projectRef, "lastUpdated", FieldValue.serverTimestamp())
                }
                
                if (donorId != null) {
                    val donorRef = getDonorsCollection().document(donorId)
                    transaction.set(donorRef, mapOf("lastContactDate" to FieldValue.serverTimestamp()), SetOptions.merge())
                }
                
                transaction.set(donationRef, donationData)
                transaction.set(summaryRef, mapOf("totalAmount" to FieldValue.increment(amount)), SetOptions.merge())
                transaction.set(db.collection("lumisphere").document("revenue"), mapOf("totalAmount" to FieldValue.increment(amount)), SetOptions.merge())
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToApproval(doc: com.google.firebase.firestore.DocumentSnapshot) = Approval(
        id = doc.id,
        type = doc.getString("type") ?: "Approval",
        account = doc.getString("account") ?: "",
        amount = doc.getLong("amount")?.toInt() ?: 0,
        priority = doc.getString("priority") ?: "Medium",
        date = (doc.get("timestamp") as? Timestamp)?.let { dateFormatter.format(it.toDate()) } ?: "Recently",
        status = doc.getString("status") ?: "Pending",
        requestedBy = doc.getString("requestedBy") ?: ""
    )

    private fun mapToExpense(doc: com.google.firebase.firestore.DocumentSnapshot) = Expense(
        id = doc.id,
        category = doc.getString("category") ?: "",
        account = doc.getString("account") ?: "",
        amount = doc.getLong("amount")?.toInt() ?: 0,
        date = (doc.get("timestamp") as? Timestamp)?.let { dateFormatter.format(it.toDate()) } ?: "Recently",
        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
        projectId = doc.getString("projectId")
    )

    private fun mapFromExpense(e: Expense) = hashMapOf(
        "category" to e.category,
        "account" to e.account,
        "amount" to e.amount,
        "timestamp" to FieldValue.serverTimestamp(),
        "projectId" to e.projectId
    )
}
