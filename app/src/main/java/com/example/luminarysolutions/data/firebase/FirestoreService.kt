package com.example.luminarysolutions.data.firebase

import android.util.Log
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.data.models.Expense
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.models.Project
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
    
    // Path: lumisphere (collection) -> projects (document) -> items (sub-collection)
    private fun getProjectsCollection() = db.collection("lumisphere")
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


    /**
     * Dashboard stats using Flow for real-time updates.
     */
    fun getDashboardStats(): Flow<DashboardStats> = callbackFlow {
        val orgCol = db.collection("lumisphere")
        
        var projectsCount = 0
        var donorsCount = 0
        var partnersCount = 0
        var expensesTotal = 0

        val emit = {
            trySend(DashboardStats(projectsCount, donorsCount, expensesTotal, partnersCount))
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

        awaitClose {
            pListener.remove()
            dListener.remove()
            partListener.remove()
            eListener.remove()
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

    fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
        val registration = getProjectsCollection().document(projectId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(mapToProjectUi(doc))
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
            groupLeaderId = doc.getString("groupLeaderId")
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
            "groupLeaderId" to project.groupLeaderId
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
}
