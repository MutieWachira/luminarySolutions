package com.example.luminarysolutions.data.firebase

import android.util.Log
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.Expense
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Locale

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    private val monthOrder = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
    
    // Path: lumisphere (collection) -> projects (document) -> items (sub-collection)
    private fun getProjectsCollection() = db.collection("lumisphere")
        .document("projects")
        .collection("items")

    // Path: luminary (collection) -> projects (document) -> items (sub-collection)
    private fun getLuminaryProjectsCollection() = db.collection("luminary")
        .document("projects")
        .collection("items")

    // Path: lumisphere (collection) -> donors (document) -> items (sub-collection)
    private fun getDonorsCollection() = db.collection("lumisphere")
        .document("donors")
        .collection("items")

    // Path: lumisphere (collection) -> expenses (document) -> items (sub-collection)
    private fun getExpensesCollection() = db.collection("lumisphere")
        .document("expenses")
        .collection("items")

    //Path: lumisphere (collection) -> partners (document) -> items (sub-collection)
    private fun getPartnersCollection() = db.collection("lumisphere")
        .document("partners")
        .collection("items")

    // Path: lumisphere (collection) -> volunteers (document) -> items (sub-collection)
    private fun getVolunteersCollection() = db.collection("lumisphere")
        .document("volunteers")
        .collection("items")

    // Path: lumisphere (collection) -> approvals (document) -> items (sub-collection)
    private fun getApprovalsCollection() = db.collection("lumisphere")
        .document("approvals")
        .collection("items")


    /**
     * Dashboard stats using Flow for real-time updates.
     */
    fun getDashboardStats(): Flow<DashboardStats> = callbackFlow {
        val orgCol = db.collection("lumisphere")
        
        var projectsCount = 0
        var donorsCount = 0
        var partnersCount = 0
        var expensesTotal = 0
        var revenueTotal = 0

        val emit = {
            trySend(DashboardStats(projectsCount, donorsCount, expensesTotal, partnersCount, revenueTotal))
        }

        val pListener = orgCol.document("projects").addSnapshotListener { doc, _ ->
            projectsCount = doc?.getLong("count")?.toInt() ?: doc?.getLong("total")?.toInt() ?: 0
            emit()
        }

        val dListener = orgCol.document("donors").addSnapshotListener { doc, _ ->
            donorsCount = doc?.getLong("count")?.toInt() ?: doc?.getLong("total")?.toInt() ?: 0
            emit()
        }

        val partListener = orgCol.document("partners").addSnapshotListener { doc, _ ->
            partnersCount = doc?.getLong("count")?.toInt() ?: doc?.getLong("total")?.toInt() ?: 0
            emit()
        }

        val eListener = orgCol.document("expenses").addSnapshotListener { doc, _ ->
            expensesTotal = doc?.getLong("totalAmount")?.toInt() ?: 0
            emit()
        }

        val rListener = orgCol.document("revenue").addSnapshotListener { doc, _ ->
            revenueTotal = doc?.getLong("totalAmount")?.toInt() ?: 0
            emit()
        }

        awaitClose {
            pListener.remove()
            dListener.remove()
            partListener.remove()
            eListener.remove()
            rListener.remove()
        }
    }

    /**
     * Fetch projects list from the nested collection.
     */
    fun getProjects(): Flow<List<Project>> = callbackFlow {
        val registration = getProjectsCollection()
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Error fetching projects: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val projects = snapshot?.documents?.mapNotNull { doc -> mapToProjectUi(doc) } ?: emptyList()
                trySend(projects)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Smart lookup for a project by ID across both lumisphere and luminary collections.
     */
    fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
        // First try the lumisphere collection (General Projects)
        val registration = getProjectsCollection().document(projectId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                if (doc != null && doc.exists()) {
                    trySend(mapToProjectUi(doc))
                } else {
                    // If not found in lumisphere, try the luminary collection (Business Projects)
                    getLuminaryProjectsCollection().document(projectId).get()
                        .addOnSuccessListener { lumDoc ->
                            if (lumDoc.exists()) {
                                trySend(mapToProjectUi(lumDoc))
                            } else {
                                trySend(null)
                            }
                        }
                        .addOnFailureListener {
                            trySend(null)
                        }
                }
            }
        awaitClose { registration.remove() }
    }

    private fun mapToProjectUi(doc: DocumentSnapshot): Project {
        val rawDate = doc.get("lastUpdated")
        val dateDisplay = when (rawDate) {
            is Timestamp -> dateFormatter.format(rawDate.toDate())
            is String -> rawDate
            else -> "Just now"
        }

        val rawTasks = doc.get("tasks") as? List<Map<String, Any>>
        val tasks = rawTasks?.map { taskMap ->
            com.example.luminarysolutions.data.models.Task(
                id = taskMap["id"] as? String ?: "",
                title = taskMap["title"] as? String ?: "",
                assignedTo = taskMap["assignedTo"] as? String ?: "",
                isDone = taskMap["isDone"] as? Boolean ?: false
            )
        } ?: emptyList()

        return Project(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed Project",
            status = doc.getString("status") ?: "Ongoing",
            budget = doc.getLong("budget")?.toInt() ?: 0,
            spent = doc.getLong("spent")?.toInt() ?: 0,
            progress = doc.getDouble("progress")?.toFloat() ?: 0f,
            lastUpdated = dateDisplay,
            imageUrl = doc.getString("imageUrl"),
            description = doc.getString("description") ?: "",
            location = doc.getString("location") ?: "",
            startDate = doc.getLong("startDate") ?: System.currentTimeMillis(),
            tasks = tasks,
            volunteers = doc.get("volunteers") as? List<String> ?: emptyList(),
            groupLeaderId = doc.getString("groupLeaderId") ?: "",
            groupLeaderIds = doc.get("groupLeaderIds") as? List<String> ?: emptyList(),
            client = doc.getString("client") ?: "",
            category = doc.getString("category") ?: ""
        )
    }

    /**
     * Add project to the nested collection and update the counter.
     */
    fun addProject(project: Project, onComplete: (Boolean) -> Unit) {
        val projectData = hashMapOf(
            "name" to project.name,
            "status" to project.status,
            "budget" to project.budget,
            "spent" to project.spent,
            "progress" to project.progress,
            "lastUpdated" to FieldValue.serverTimestamp(),
            "imageUrl" to project.imageUrl,
            "description" to project.description,
            "location" to project.location,
            "startDate" to project.startDate,
            "tasks" to project.tasks.map { task ->
                mapOf(
                    "id" to task.id,
                    "title" to task.title,
                    "assignedTo" to task.assignedTo,
                    "isDone" to task.isDone
                )
            },
            "volunteers" to project.volunteers,
            "groupLeaderId" to project.groupLeaderId,
            "groupLeaderIds" to project.groupLeaderIds,
            "client" to project.client,
            "category" to project.category
        )
        
        getProjectsCollection().add(projectData)
            .addOnSuccessListener { 
                Log.d("FirestoreService", "Project added successfully")
                val statsRef = db.collection("lumisphere").document("projects")
                statsRef.update("count", FieldValue.increment(1))
                    .addOnFailureListener {
                        statsRef.set(mapOf("count" to 1), com.google.firebase.firestore.SetOptions.merge())
                    }
                onComplete(true) 
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "Error adding project: ${e.message}")
                onComplete(false) 
            }
    }

    /**
     * Luminary Specific Projects
     */
    fun getLuminaryProjects(): Flow<List<Project>> = callbackFlow {
        val registration = getLuminaryProjectsCollection()
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val projects = snapshot?.documents?.mapNotNull { doc -> mapToProjectUi(doc) } ?: emptyList()
                trySend(projects)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Team / Users Section
     */
    fun getTeamMembers(): Flow<List<User>> = callbackFlow {
        val registration = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed",
                        email = doc.getString("email") ?: "",
                        role = com.example.luminarysolutions.ui.auth.safeValueOf(doc.getString("role")),
                        enabled = doc.getBoolean("enabled") ?: true
                    )
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    fun addLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        val projectData = hashMapOf(
            "name" to project.name,
            "status" to project.status,
            "budget" to project.budget,
            "spent" to project.spent,
            "progress" to project.progress,
            "lastUpdated" to FieldValue.serverTimestamp(),
            "imageUrl" to project.imageUrl,
            "description" to project.description,
            "location" to project.location,
            "startDate" to project.startDate,
            "tasks" to emptyList<Map<String, Any>>(),
            "volunteers" to emptyList<String>(),
            "groupLeaderId" to project.groupLeaderId,
            "groupLeaderIds" to project.groupLeaderIds,
            "client" to project.client,
            "category" to project.category
        )

        getLuminaryProjectsCollection().add(projectData)
            .addOnSuccessListener {
                val statsRef = db.collection("luminary").document("projects")
                statsRef.update("count", FieldValue.increment(1))
                    .addOnFailureListener {
                        statsRef.set(mapOf("count" to 1), com.google.firebase.firestore.SetOptions.merge())
                    }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        getLuminaryProjectsCollection().document(projectId).delete()
            .addOnSuccessListener {
                val statsRef = db.collection("luminary").document("projects")
                statsRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        statsRef.update("count", FieldValue.increment(-1))
                    }
                }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Updates an existing project in the luminary collection.
     */
    fun updateLuminaryProject(project: Project, onComplete: (Boolean) -> Unit) {
        if (project.id.isEmpty()) {
            onComplete(false)
            return
        }

        val projectData = hashMapOf(
            "name" to project.name,
            "status" to project.status,
            "budget" to project.budget,
            "spent" to project.spent,
            "progress" to project.progress,
            "lastUpdated" to FieldValue.serverTimestamp(),
            "imageUrl" to project.imageUrl,
            "description" to project.description,
            "location" to project.location,
            "client" to project.client,
            "category" to project.category,
            "groupLeaderId" to project.groupLeaderId,
            "groupLeaderIds" to project.groupLeaderIds
        )

        getLuminaryProjectsCollection().document(project.id).update(projectData as Map<String, Any>)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Fetches a single luminary project by ID.
     */
    fun getLuminaryProjectById(projectId: String): Flow<Project?> = callbackFlow {
        val registration = getLuminaryProjectsCollection().document(projectId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    trySend(mapToProjectUi(doc))
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        val projectRef = getProjectsCollection().document(projectId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(projectRef)
            val tasks = snapshot.get("tasks") as? List<Map<String, Any>> ?: emptyList()
            val updatedTasks = tasks.map { task ->
                if (task["id"] == taskId) {
                    val updatedTask = task.toMutableMap()
                    updatedTask["isDone"] = isDone
                    updatedTask
                } else {
                    task
                }
            }
            
            val doneCount = updatedTasks.count { it["isDone"] == true }
            val totalCount = updatedTasks.size
            val progress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f
            
            transaction.update(projectRef, "tasks", updatedTasks)
            transaction.update(projectRef, "progress", progress)
            transaction.update(projectRef, "lastUpdated", FieldValue.serverTimestamp())
        }.addOnSuccessListener { onComplete(true) }
         .addOnFailureListener { onComplete(false) }
    }

    fun addTaskToProject(projectId: String, task: com.example.luminarysolutions.data.models.Task, onComplete: (Boolean) -> Unit) {
        val projectRef = getProjectsCollection().document(projectId)
        val taskData = mapOf(
            "id" to task.id,
            "title" to task.title,
            "assignedTo" to task.assignedTo,
            "isDone" to task.isDone
        )
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(projectRef)
            val tasks = snapshot.get("tasks") as? List<Map<String, Any>> ?: emptyList()
            val updatedTasks = tasks + taskData
            
            val doneCount = updatedTasks.count { it["isDone"] == true }
            val totalCount = updatedTasks.size
            val progress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f
            
            transaction.update(projectRef, "tasks", updatedTasks)
            transaction.update(projectRef, "progress", progress)
            transaction.update(projectRef, "lastUpdated", FieldValue.serverTimestamp())
        }.addOnSuccessListener { onComplete(true) }
         .addOnFailureListener { onComplete(false) }
    }

    fun getVolunteers(): Flow<List<com.example.luminarysolutions.data.models.Volunteer>> = callbackFlow {
        val registration = getVolunteersCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val volunteers = snapshot?.documents?.mapNotNull { doc ->
                    com.example.luminarysolutions.data.models.Volunteer(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed",
                        email = doc.getString("email") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        status = doc.getString("status") ?: "Available"
                    )
                } ?: emptyList()
                trySend(volunteers)
            }
        awaitClose { registration.remove() }
    }

    fun assignGroupLeader(projectId: String, leaderId: String, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId)
            .update("groupLeaderId", leaderId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Donors Section
     */
    fun getDonors(): Flow<List<Donor>> = callbackFlow {
        val registration = getDonorsCollection()
            .orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Error fetching donors: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val donors = snapshot?.documents?.mapNotNull { doc -> mapToDonor(doc) } ?: emptyList()
                trySend(donors)
            }
        awaitClose { registration.remove() }
    }

    private fun mapToDonor(doc: DocumentSnapshot): Donor {
        val rawDate = doc.get("lastContactDate")
        val dateDisplay = when (rawDate) {
            is Timestamp -> {
                val now = Timestamp.now().seconds
                val diff = now - rawDate.seconds
                when {
                    diff < 3600 -> "Just now"
                    diff < 86400 -> "${diff / 3600}h ago"
                    diff < 172800 -> "Yesterday"
                    else -> dateFormatter.format(rawDate.toDate())
                }
            }
            is String -> rawDate
            else -> "No contact yet"
        }

        return Donor(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed Donor",
            type = doc.getString("type") ?: "Donor",
            status = doc.getString("status") ?: "Active",
            valueOrNote = doc.getString("valueOrNote") ?: "—",
            lastContact = "Last contact: $dateDisplay"
        )
    }

    fun addDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        val donorData = hashMapOf(
            "name" to donor.name,
            "type" to donor.type,
            "status" to donor.status,
            "valueOrNote" to donor.valueOrNote,
            "lastContactDate" to FieldValue.serverTimestamp()
        )

        getDonorsCollection().add(donorData)
            .addOnSuccessListener {
                val statsRef = db.collection("lumisphere").document("donors")
                statsRef.update("count", FieldValue.increment(1))
                    .addOnFailureListener {
                        statsRef.set(mapOf("count" to 1), com.google.firebase.firestore.SetOptions.merge())
                    }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Partner Section
     */
    fun getPartners(): Flow<List<Partner>> = callbackFlow {
        val registration = getPartnersCollection()
            .orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Error fetching partners: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val partners = snapshot?.documents?.mapNotNull { doc -> mapToPartner(doc) } ?: emptyList()
                trySend(partners)
            }
        awaitClose { registration.remove() }
    }

    private fun mapToPartner(doc: DocumentSnapshot): Partner {
        val rawDate = doc.get("lastContactDate")
        val dateDisplay = when (rawDate) {
            is Timestamp -> {
                val now = Timestamp.now().seconds
                val diff = now - rawDate.seconds
                when {
                    diff < 3600 -> "Just now"
                    diff < 86400 -> "${diff / 3600}h ago"
                    diff < 172800 -> "Yesterday"
                    else -> dateFormatter.format(rawDate.toDate())
                }
            }
            is String -> rawDate
            else -> "No contact yet"
        }

        return Partner(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed Donor",
            type = doc.getString("type") ?: "Partner",
            status = doc.getString("status") ?: "Active",
            valueOrNote = doc.getString("valueOrNote") ?: "—",
            lastContact = "Last contact: $dateDisplay"
        )
    }

    fun addPartner(partner: Partner, onComplete: (Boolean) -> Unit) {
        val partnerData = hashMapOf(
            "name" to partner.name,
            "type" to partner.type,
            "status" to partner.status,
            "valueOrNote" to partner.valueOrNote,
            "lastContactDate" to FieldValue.serverTimestamp()
        )

        getPartnersCollection().add(partnerData)
            .addOnSuccessListener {
                val statsRef = db.collection("lumisphere").document("partners")
                statsRef.update("count", FieldValue.increment(1))
                    .addOnFailureListener {
                        statsRef.set(mapOf("count" to 1), com.google.firebase.firestore.SetOptions.merge())
                    }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }



    /**
     * Expenses Section
     */
    fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val registration = getExpensesCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    val rawDate = doc.get("timestamp")
                    val dateStr = when (rawDate) {
                        is Timestamp -> dateFormatter.format(rawDate.toDate())
                        else -> "Recently"
                    }
                    Expense(
                        id = doc.id,
                        category = doc.getString("category") ?: "",
                        account = doc.getString("account") ?: "",
                        amount = doc.getLong("amount")?.toInt() ?: 0,
                        date = dateStr,
                        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                        projectId = doc.getString("projectId")
                    )
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { registration.remove() }
    }

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "category" to expense.category,
            "account" to expense.account,
            "amount" to expense.amount,
            "timestamp" to FieldValue.serverTimestamp(),
            "projectId" to expense.projectId
        )
        getExpensesCollection().add(data)
            .addOnSuccessListener {
                val statsRef = db.collection("lumisphere").document("expenses")
                statsRef.update("totalAmount", FieldValue.increment(expense.amount.toLong()))
                    .addOnFailureListener {
                        statsRef.set(mapOf("totalAmount" to expense.amount.toLong()), com.google.firebase.firestore.SetOptions.merge())
                    }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Approvals Section
     */
    fun getApprovals(): Flow<List<Approval>> = callbackFlow {
        val registration = getApprovalsCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val approvals = snapshot?.documents?.mapNotNull { doc ->
                    val rawDate = doc.get("timestamp")
                    val dateStr = when (rawDate) {
                        is Timestamp -> dateFormatter.format(rawDate.toDate())
                        else -> "Recently"
                    }
                    Approval(
                        id = doc.id,
                        type = doc.getString("type") ?: "Approval",
                        account = doc.getString("account") ?: "",
                        amount = doc.getLong("amount")?.toInt() ?: 0,
                        priority = doc.getString("priority") ?: "Medium",
                        date = dateStr,
                        status = doc.getString("status") ?: "Pending",
                        requestedBy = doc.getString("requestedBy") ?: ""
                    )
                } ?: emptyList()
                trySend(approvals)
            }
        awaitClose { registration.remove() }
    }

    fun addApproval(approval: Approval, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "type" to approval.type,
            "account" to approval.account,
            "amount" to approval.amount,
            "priority" to approval.priority,
            "status" to approval.status,
            "requestedBy" to approval.requestedBy,
            "timestamp" to FieldValue.serverTimestamp()
        )
        getApprovalsCollection().add(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * luminary dashboards for selected tabs
     */

    /**
     * OverView Tab - Fetches real-time financial stats including monthly data for a specific year.
     * Logic: Listens to /luminary/financials/years/{year}/months/ documents.
     */
    fun getLumDashStats(year: Int): Flow<com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats> = callbackFlow {
        // Industry Practice: Collection names are case-sensitive. 
        val orgCol = db.collection("luminary")

        var projectsCount = 0
        var totalExpenses = 0
        var totalRevenue = 0
        var activeClientCount = 0
        var monthlyStats = emptyList<com.example.luminarysolutions.data.firebase.MonthlyFinancialStats>()

        val emit = {
            // Logic: If top-level totals are missing, sum them up from monthly stats
            val displayRevenue = if (totalRevenue == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.revenue } else totalRevenue
            val displayExpenses = if (totalExpenses == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.expenses } else totalExpenses
            val totalProfit = displayRevenue - displayExpenses
            
            trySend(com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats(
                totalRevenue = displayRevenue,
                totalExpenses = displayExpenses,
                totalProfit = totalProfit,
                totalProjects = projectsCount,
                totalActiveClient = activeClientCount,
                monthlyStats = monthlyStats
            ))
        }

        // Listen for static dashboard metrics
        val pListener = orgCol.document("projects").addSnapshotListener { doc, _ ->
            projectsCount = (doc?.get("count") as? Number)?.toInt() ?: 0
            emit()
        }
        val rListener = orgCol.document("revenue").addSnapshotListener { doc, _ ->
            totalRevenue = (doc?.get("revenue") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0
            emit()
        }
        val eListener = orgCol.document("expenses").addSnapshotListener { doc, _ ->
            totalExpenses = (doc?.get("expense") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0
            emit()
        }
        val cListener = orgCol.document("clients").addSnapshotListener { doc, _ ->
            activeClientCount = (doc?.get("count") as? Number)?.toInt() ?: 0
            emit()
        }

        // Path: /luminary/financials/years/2026/months/
        val mListener = orgCol.document("financials")
            .collection("years")
            .document(year.toString())
            .collection("months")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "DB Error at $year/months: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    Log.d("FirestoreService", "Found ${snapshot.size()} documents in $year/months")
                    val unsortedStats = snapshot.documents.mapNotNull { doc ->
                        // Handle both Number and potential String types for robustness
                        val rev = (doc.get("revenue") as? Number)?.toInt() ?: doc.getString("revenue")?.toIntOrNull() ?: 0
                        val exp = (doc.get("expense") as? Number)?.toInt() ?: doc.getString("expense")?.toIntOrNull() ?: 0
                        
                        Log.d("FirestoreService", "Month document ${doc.id}: rev=$rev, exp=$exp")
                        
                        com.example.luminarysolutions.data.firebase.MonthlyFinancialStats(
                            month = doc.id.lowercase(), 
                            revenue = rev,
                            expenses = exp 
                        )
                    }

                    // Sort chronologically (Jan -> Dec)
                    monthlyStats = unsortedStats.sortedBy { monthOrder.indexOf(it.month) }
                    Log.d("FirestoreService", "Successfully updated monthlyStats. Size: ${monthlyStats.size}")
                    emit()
                } else {
                    Log.d("FirestoreService", "Snapshot is empty for path: luminary/financials/years/$year/months")
                    monthlyStats = emptyList()
                    emit()
                }
            }

        awaitClose {
            pListener.remove()
            eListener.remove()
            rListener.remove()
            cListener.remove()
            mListener.remove()
        }
    }

    fun getDocuments(): Flow<List<com.example.luminarysolutions.data.models.Document>> = callbackFlow {
        val registration = db.collection("luminary").document("documents").collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    com.example.luminarysolutions.data.models.Document(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "PDF",
                        uploader = doc.getString("uploader") ?: "System",
                        date = doc.getString("date") ?: "Today",
                        size = doc.getString("size") ?: "0 KB"
                    )
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }


}
