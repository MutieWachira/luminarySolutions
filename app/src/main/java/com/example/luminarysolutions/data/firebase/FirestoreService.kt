package com.example.luminarysolutions.data.firebase

import android.util.Log
import com.example.luminarysolutions.data.models.*
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * FirestoreService: Centralized Firestore operations for Luminary and LumiSphere.
 * Follows Clean Architecture principles by providing data streams (Flows) and 
 * handling data mapping internally.
 */
object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    private val monthOrder = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

    // =========================================================================================
    // LUMINARY SECTION (Business & Internal Operations)
    // =========================================================================================

    // Collections
    private fun getLuminaryProjectsCollection() = db.collection("luminary").document("freelances").collection("items")
    private fun getTeamsCollection() = db.collection("luminary").document("teams").collection("items")
    private fun getDocumentsCollection() = db.collection("luminary").document("documents").collection("items")

    /**
     * Fetches real-time financial stats for Luminary for a specific year.
     */
    fun getLumDashStats(year: Int): Flow<lumOverviewDashboardStats> = callbackFlow {
        val orgCol = db.collection("luminary")
        var projectsCount = 0
        var totalExpenses = 0
        var totalRevenue = 0
        var activeClientCount = 0
        var monthlyStats = emptyList<MonthlyFinancialStats>()

        val emit = {
            val displayRevenue = if (totalRevenue == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.revenue } else totalRevenue
            val displayExpenses = if (totalExpenses == 0 && monthlyStats.isNotEmpty()) monthlyStats.sumOf { it.expenses } else totalExpenses
            trySend(lumOverviewDashboardStats(
                totalRevenue = displayRevenue,
                totalExpenses = displayExpenses,
                totalProfit = displayRevenue - displayExpenses,
                totalProjects = projectsCount,
                totalActiveClient = activeClientCount,
                monthlyStats = monthlyStats
            ))
        }

        val pListener = orgCol.document("freelances").addSnapshotListener { doc, error ->
            if (error != null) return@addSnapshotListener
            projectsCount = (doc?.get("count") as? Number)?.toInt() ?: 0
            emit()
        }
        val rListener = orgCol.document("revenue").addSnapshotListener { doc, error ->
            if (error != null) return@addSnapshotListener
            totalRevenue = (doc?.get("revenue") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0
            emit()
        }
        val eListener = orgCol.document("expenses").addSnapshotListener { doc, error ->
            if (error != null) return@addSnapshotListener
            totalExpenses = (doc?.get("expense") as? Number)?.toInt() ?: (doc?.get("totalAmount") as? Number)?.toInt() ?: 0
            emit()
        }
        val cListener = orgCol.document("clients").addSnapshotListener { doc, error ->
            if (error != null) return@addSnapshotListener
            activeClientCount = (doc?.get("count") as? Number)?.toInt() ?: 0
            emit()
        }

        val mListener = orgCol.document("financials").collection("years").document(year.toString()).collection("months")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
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

        awaitClose {
            pListener.remove(); rListener.remove(); eListener.remove(); cListener.remove(); mListener.remove()
        }
    }

    /**
     * Luminary Projects (Freelance/Business)
     */
    fun getLuminaryProjects(): Flow<List<Freelance>> = callbackFlow {
        val registration = getLuminaryProjectsCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { mapToFreelanceUi(it) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun getLuminaryProjectById(projectId: String): Flow<Freelance?> = callbackFlow {
        val registration = getLuminaryProjectsCollection().document(projectId)
            .addSnapshotListener { doc, _ -> trySend(doc?.let { if (it.exists()) mapToFreelanceUi(it) else null }) }
        awaitClose { registration.remove() }
    }

    fun addLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        getLuminaryProjectsCollection().add(mapFromFreelance(freelance))
            .addOnSuccessListener {
                db.collection("luminary").document("freelances").update("count", FieldValue.increment(1))
                    .addOnFailureListener { db.collection("luminary").document("freelances").set(mapOf("count" to 1), SetOptions.merge()) }
                onComplete(true)
            }.addOnFailureListener { onComplete(false) }
    }

    fun updateLuminaryProject(freelance: Freelance, onComplete: (Boolean) -> Unit) {
        if (freelance.id.isEmpty()) { onComplete(false); return }
        getLuminaryProjectsCollection().document(freelance.id).update(mapFromFreelance(freelance) as Map<String, Any>)
            .addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun deleteLuminaryProject(projectId: String, onComplete: (Boolean) -> Unit) {
        getLuminaryProjectsCollection().document(projectId).delete()
            .addOnSuccessListener {
                db.collection("luminary").document("freelances").update("count", FieldValue.increment(-1))
                onComplete(true)
            }.addOnFailureListener { onComplete(false) }
    }

    /**
     * Team Management (Luminary)
     */
    fun getTeams(): Flow<List<Team>> = callbackFlow {
        val registration = getTeamsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { mapToTeam(it) } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches a specific team member's profile by their email in real-time.
     * This is more reliable than UID if the Firestore document IDs don't match Auth UIDs.
     */
    fun getTeamProfileByEmail(email: String): Flow<Team?> = callbackFlow {
        val registration = getTeamsCollection()
            .whereEqualTo("email", email)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.firstOrNull()?.let { mapToTeam(it) })
            }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches freelance projects where the current team member is assigned in real-time.
     * Supports multiple identifiers (e.g. internal ID and Auth UID) and checks for task assignments.
     */
    fun getAssignedFreelanceProjects(userIds: List<String>): Flow<List<Freelance>> = callbackFlow {
        val registration = getLuminaryProjectsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val projects = snapshot?.documents?.mapNotNull { mapToFreelanceUi(it) } ?: emptyList()
                val assigned = projects.filter { freelance ->
                    userIds.any { id ->
                        freelance.teamIds.contains(id) || 
                        freelance.tasks.any { task -> task.assignedToIds.contains(id) }
                    }
                }
                trySend(assigned)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches all projects where the user is assigned or has tasks to monitor.
     * Supports multiple identifiers (e.g. internal ID and Auth UID).
     */
    fun getAssignedProjects(userIds: List<String>): Flow<List<Project>> = callbackFlow {
        val registration = getProjectsCollection() // This is lumisphere
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val projects = snapshot?.documents?.mapNotNull { mapToProjectUi(it) } ?: emptyList()
                val assigned = projects.filter { project ->
                    userIds.any { id ->
                        project.groupLeaderIds.contains(id) || 
                        project.teamMemberIds.contains(id) || 
                        project.volunteers.contains(id) ||
                        project.groupLeaderId == id ||
                        project.tasks.any { task -> task.assignedToIds.contains(id) }
                    }
                }
                trySend(assigned)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches specific team members by their unique IDs.
     * Used for retrieving assigned members for a specific project/freelance.
     */
    fun getTeamsByIds(ids: List<String>): Flow<List<Team>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        // Firestore 'whereIn' is limited to 10-30 items depending on version, 
        // but for team assignment this is usually sufficient.
        val registration = getTeamsCollection()
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { mapToTeam(it) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun addTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        getTeamsCollection().add(mapFromTeam(team)).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun updateTeamMember(team: Team, onComplete: (Boolean) -> Unit) {
        if (team.id.isEmpty()) { onComplete(false); return }
        val updates = mapFromTeam(team).apply {
            remove("createdAt") // Prevent resetting creation date on updates
        }
        getTeamsCollection().document(team.id).update(updates as Map<String, Any>)
            .addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    /**
     * Surgically updates a team member's profile.
     * Synchronizes changes with the global 'users' collection.
     */
    fun updateTeamProfile(team: Team, onComplete: (Boolean) -> Unit) {
        if (team.id.isEmpty()) { onComplete(false); return }
        val updates = hashMapOf<String, Any?>(
            "name" to team.name,
            "phone" to team.phone,
            "phoneNumber" to team.phone, // Sync with User model
            "bio" to team.bio,
            "gender" to team.gender,
            "imageUrl" to team.imageUrl,
            "profileImageUrl" to team.imageUrl, // Sync with User model
            "isTwoFactorEnabled" to team.isTwoFactorEnabled
        )
        getTeamsCollection().document(team.id).update(updates)
            .addOnSuccessListener { 
                db.collection("users").document(team.id).update(updates)
                onComplete(true) 
            }.addOnFailureListener { onComplete(false) }
    }

    fun deleteTeamMember(teamId: String, onComplete: (Boolean) -> Unit) {
        getTeamsCollection().document(teamId).delete().addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun getTeamCulture(): Flow<TeamCulture> = callbackFlow {
        val registration = db.collection("luminary").document("culture").addSnapshotListener { doc, _ ->
            trySend(doc?.takeIf { it.exists() }?.let {
                TeamCulture(
                    diversityRate = it.getString("diversityRate") ?: "38%",
                    satisfactionScore = it.getString("satisfactionScore") ?: "4.6/5",
                    trainingPrograms = it.getLong("trainingPrograms")?.toInt() ?: 24
                )
            } ?: TeamCulture())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Documents (Luminary)
     */
    fun getDocuments(): Flow<List<Document>> = callbackFlow {
        val registration = getDocumentsCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ -> trySend(snapshot?.documents?.mapNotNull { mapToDocument(it) } ?: emptyList()) }
        awaitClose { registration.remove() }
    }

    fun addDocument(document: Document, onComplete: (Boolean) -> Unit) {
        getDocumentsCollection().add(mapFromDocument(document)).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun deleteDocument(docId: String, onComplete: (Boolean) -> Unit) {
        getDocumentsCollection().document(docId).delete().addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    // =========================================================================================
    // LUMISPHERE SECTION (NGO & Impact Operations)
    // =========================================================================================

    // Collections
    private fun getProjectsCollection() = db.collection("lumisphere").document("projects").collection("items")
    private fun getDonorsCollection() = db.collection("lumisphere").document("donors").collection("items")
    private fun getExpensesCollection() = db.collection("lumisphere").document("expenses").collection("items")
    private fun getPartnersCollection() = db.collection("lumisphere").document("partners").collection("items")
    private fun getVolunteersCollection() = db.collection("lumisphere").document("volunteers").collection("items")
    private fun getApprovalsCollection() = db.collection("lumisphere").document("approvals").collection("items")
    private fun getAchievementsCollection() = db.collection("lumisphere").document("achievements").collection("items")
    private fun getEventsCollection() = db.collection("lumisphere").document("events").collection("items")
    private fun getNotificationsCollection() = db.collection("notifications")

    /**
     * Fetches real-time impact stats for LumiSphere.
     */
    fun getLumiSphereDashStats(year: Int): Flow<lumiSphereOverviewDashboardStats> = callbackFlow {
        val orgCol = db.collection("lumisphere")
        var programsCount = 0; var totalDonations = 0; var totalSpent = 0; var totalBeneficiaries = 0; var monthlyStats = emptyList<MonthlyImpactStats>()

        val emit = {
            trySend(lumiSphereOverviewDashboardStats(
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

    /**
     * Legacy Dashboard Stats
     */
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

    /**
     * LumiSphere Projects (Programs/NGO)
     */
    fun getProjects(): Flow<List<Project>> = callbackFlow {
        val registration = getProjectsCollection().orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error -> 
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { mapToProjectUi(it) } ?: emptyList()) 
            }
        awaitClose { registration.remove() }
    }

    fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
        val registration = getProjectsCollection().document(projectId).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) trySend(mapToProjectUi(doc))
            else {
                getLuminaryProjectsCollection().document(projectId).get().addOnSuccessListener { lumDoc ->
                    trySend(if (lumDoc.exists()) mapToProjectUi(lumDoc) else null)
                }.addOnFailureListener { trySend(null) }
            }
        }
        awaitClose { registration.remove() }
    }

    fun addProject(project: Project, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().add(mapFromProject(project)).addOnSuccessListener {
            db.collection("lumisphere").document("projects").update("count", FieldValue.increment(1))
                .addOnFailureListener { db.collection("lumisphere").document("projects").set(mapOf("count" to 1), SetOptions.merge()) }
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    fun updateProject(project: Project, onComplete: (Boolean) -> Unit) {
        if (project.id.isEmpty()) { onComplete(false); return }
        getProjectsCollection().document(project.id).update(
            mapFromProject(project).apply {
                this["lastUpdated"] = FieldValue.serverTimestamp()
            } as Map<String, Any>
        ).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun deleteProject(projectId: String, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).delete().addOnSuccessListener {
            db.collection("lumisphere").document("projects").update("count", FieldValue.increment(-1))
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    fun updateProjectTeamMembers(projectId: String, teamMemberIds: List<String>, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).update(
            mapOf(
                "teamMemberIds" to teamMemberIds,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, isFreelanceHint: Boolean? = null, onComplete: (Boolean) -> Unit) {
        val updateLogic: (DocumentSnapshot, Boolean) -> Unit = { doc, isLuminary ->
            if (doc.exists()) {
                val tasks = if (isLuminary) mapToFreelanceUi(doc).tasks else mapToProjectUi(doc).tasks
                val updatedTasks = tasks.map { 
                    if (it.id == taskId) it.copy(isDone = isDone) else it 
                }
                if (isLuminary) updateFreelanceTasks(projectId, updatedTasks, onComplete)
                else updateProjectTasks(projectId, updatedTasks, onComplete)
            } else {
                onComplete(false)
            }
        }

        when (isFreelanceHint) {
            true -> getLuminaryProjectsCollection().document(projectId).get()
                .addOnSuccessListener { updateLogic(it, true) }
                .addOnFailureListener { onComplete(false) }
            false -> getProjectsCollection().document(projectId).get()
                .addOnSuccessListener { updateLogic(it, false) }
                .addOnFailureListener { onComplete(false) }
            null -> {
                getProjectsCollection().document(projectId).get().addOnSuccessListener { doc ->
                    if (doc.exists()) updateLogic(doc, false)
                    else {
                        getLuminaryProjectsCollection().document(projectId).get()
                            .addOnSuccessListener { lumDoc -> updateLogic(lumDoc, true) }
                            .addOnFailureListener { onComplete(false) }
                    }
                }.addOnFailureListener { onComplete(false) }
            }
        }
    }

    fun updateProjectTasks(projectId: String, tasks: List<Task>, onComplete: (Boolean) -> Unit) {
        val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
        getProjectsCollection().document(projectId).update(
            mapOf(
                "tasks" to tasks.map { mapFromTask(it) },
                "progress" to progress,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun updateFreelanceTasks(freelanceId: String, tasks: List<Task>, onComplete: (Boolean) -> Unit) {
        val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
        getLuminaryProjectsCollection().document(freelanceId).update(
            mapOf(
                "tasks" to tasks.map { mapFromTask(it) },
                "progress" to progress,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun addTasksToProject(projectId: String, newTasks: List<Task>, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                newTasks.forEach { tasks.add(mapFromTask(it)) }
                updateProjectTasks(projectId, tasks.map { 
                    Task(
                        id = it["id"] as? String ?: "",
                        title = it["title"] as? String ?: "",
                        description = it["description"] as? String ?: "",
                        assignedToIds = it["assignedToIds"] as? List<String> ?: (it["assignedToId"] as? String)?.let { listOf(it) } ?: emptyList(),
                        assignedToNames = it["assignedToNames"] as? List<String> ?: (it["assignedToName"] as? String)?.let { listOf(it) } ?: emptyList(),
                        assignedById = it["assignedById"] as? String ?: "",
                        assigneeType = try { AssigneeType.valueOf(it["assigneeType"] as? String ?: "TEAM") } catch (e: Exception) { AssigneeType.TEAM },
                        deadline = (it["deadline"] as? Number)?.toLong() ?: 0L,
                        isDone = it["isDone"] as? Boolean ?: false,
                        createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L
                    )
                }, onComplete)
            } else {
                getLuminaryProjectsCollection().document(projectId).get().addOnSuccessListener { lumDoc ->
                    if (lumDoc.exists()) {
                        val tasks = (lumDoc.get("tasks") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                        newTasks.forEach { tasks.add(mapFromTask(it)) }
                        updateFreelanceTasks(projectId, tasks.map { 
                            Task(
                                id = it["id"] as? String ?: "",
                                title = it["title"] as? String ?: "",
                                description = it["description"] as? String ?: "",
                                assignedToIds = it["assignedToIds"] as? List<String> ?: (it["assignedToId"] as? String)?.let { listOf(it) } ?: emptyList(),
                                assignedToNames = it["assignedToNames"] as? List<String> ?: (it["assignedToName"] as? String)?.let { listOf(it) } ?: emptyList(),
                                assignedById = it["assignedById"] as? String ?: "",
                                assigneeType = try { AssigneeType.valueOf(it["assigneeType"] as? String ?: "TEAM") } catch (e: Exception) { AssigneeType.TEAM },
                                deadline = (it["deadline"] as? Number)?.toLong() ?: 0L,
                                isDone = it["isDone"] as? Boolean ?: false,
                                createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L
                            )
                        }, onComplete)
                    } else onComplete(false)
                }
            }
        }
    }

    fun addTaskToProject(projectId: String, task: Task, onComplete: (Boolean) -> Unit) {
        addTasksToProject(projectId, listOf(task), onComplete)
    }

    fun assignGroupLeader(projectId: String, leaderId: String, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).update(
            mapOf(
                "groupLeaderId" to leaderId,
                "groupLeaderIds" to FieldValue.arrayUnion(leaderId),
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    /**
     * Donors & Partners
     */
    fun getDonors(): Flow<List<Donor>> = callbackFlow {
        val reg = getDonorsCollection().orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToDonor(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    /**
     * Returns a paginated stream of donors.
     */
    fun getDonorsPaginated(lastDocument: DocumentSnapshot?, pageSize: Long): Flow<Pair<List<Donor>, DocumentSnapshot?>> = callbackFlow {
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

    fun addDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        getDonorsCollection().add(mapFromDonor(donor)).addOnSuccessListener {
            db.collection("lumisphere").document("donors").update("count", FieldValue.increment(1))
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    fun updateDonor(donor: Donor, onComplete: (Boolean) -> Unit) {
        if (donor.id.isEmpty()) { onComplete(false); return }
        getDonorsCollection().document(donor.id).update(mapFromDonor(donor) as Map<String, Any>)
            .addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun deleteDonor(donorId: String, onComplete: (Boolean) -> Unit) {
        getDonorsCollection().document(donorId).delete().addOnSuccessListener {
            db.collection("lumisphere").document("donors").update("count", FieldValue.increment(-1))
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    fun getPartners(): Flow<List<Partner>> = callbackFlow {
        val reg = getPartnersCollection().orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToPartner(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun addPartner(partner: Partner, onComplete: (Boolean) -> Unit) {
        getPartnersCollection().add(mapFromPartner(partner)).addOnSuccessListener {
            db.collection("lumisphere").document("partners").update("count", FieldValue.increment(1))
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    /**
     * Expenses & Approvals
     */
    fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val reg = getExpensesCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToExpense(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        getExpensesCollection().add(mapFromExpense(expense)).addOnSuccessListener {
            db.collection("lumisphere").document("expenses").update("totalAmount", FieldValue.increment(expense.amount.toLong()))
            onComplete(true)
        }.addOnFailureListener { onComplete(false) }
    }

    fun getApprovals(): Flow<List<Approval>> = callbackFlow {
        val reg = getApprovalsCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToApproval(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun addApproval(approval: Approval, onComplete: (Boolean) -> Unit) {
        getApprovalsCollection().add(mapFromApproval(approval)).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    /**
     * Volunteers
     */
    fun getVolunteers(): Flow<List<Volunteer>> = callbackFlow {
        val reg = getVolunteersCollection()
            .whereEqualTo("status", "Approved")
            .addSnapshotListener { snp, _ ->
            trySend(snp?.documents?.mapNotNull { mapToVolunteer(it) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getVolunteerApplications(): Flow<List<Volunteer>> = callbackFlow {
        val reg = getVolunteersCollection()
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snp, _ ->
                trySend(snp?.documents?.mapNotNull { mapToVolunteer(it) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun addVolunteerApplication(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        getVolunteersCollection().add(mapFromVolunteer(volunteer))
            .addOnSuccessListener { 
                Log.d("FirestoreService", "Volunteer application submitted successfully: ${it.id}")
                onComplete(true) 
            }
            .addOnFailureListener { e -> 
                Log.e("FirestoreService", "Error submitting volunteer application", e)
                onComplete(false) 
            }
    }

    fun updateVolunteerStatus(volunteerId: String, status: String, onComplete: (Boolean) -> Unit) {
        getVolunteersCollection().document(volunteerId).update("status", status)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateVolunteer(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        if (volunteer.id.isEmpty()) { onComplete(false); return }
        
        val batch = db.batch()
        
        // 1. Update volunteers collection
        batch.set(getVolunteersCollection().document(volunteer.id), mapFromVolunteer(volunteer), SetOptions.merge())
        
        // 2. Sync with users collection
        val userUpdates = hashMapOf(
            "name" to volunteer.name,
            "email" to volunteer.email,
            "phoneNumber" to volunteer.phoneNumber,
            "profileImageUrl" to volunteer.profileImageUrl
        )
        batch.set(db.collection("users").document(volunteer.id), userUpdates, SetOptions.merge())
        
        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteVolunteer(volunteerId: String, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()
        
        // 1. Projects: Remove from volunteers list, leadership roles, and task assignments
        getProjectsCollection().whereArrayContains("volunteers", volunteerId).get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "volunteers", FieldValue.arrayRemove(volunteerId))
                    
                    // Cleanup other potential links in the same document
                    batch.update(doc.reference, "groupLeaderIds", FieldValue.arrayRemove(volunteerId))
                    batch.update(doc.reference, "teamMemberIds", FieldValue.arrayRemove(volunteerId))
                    if (doc.getString("groupLeaderId") == volunteerId) {
                        batch.update(doc.reference, "groupLeaderId", "")
                    }
                    
                    val tasks = doc.get("tasks") as? List<Map<String, Any>>
                    if (tasks != null) {
                        var tasksChanged = false
                        val updatedTasks = tasks.map { task ->
                            val assignedToIds = (task["assignedToIds"] as? List<String>)?.toMutableList() ?: mutableListOf()
                            if (assignedToIds.contains(volunteerId)) {
                                tasksChanged = true
                                assignedToIds.remove(volunteerId)
                                task.toMutableMap().apply { this["assignedToIds"] = assignedToIds }
                            } else task
                        }
                        if (tasksChanged) batch.update(doc.reference, "tasks", updatedTasks)
                    }
                }
                
                // 2. Events: Remove from attendees list
                getEventsCollection().whereArrayContains("attendees", volunteerId).get().addOnSuccessListener { eventSnapshot ->
                    eventSnapshot.documents.forEach { eventDoc ->
                        batch.update(eventDoc.reference, "attendees", FieldValue.arrayRemove(volunteerId))
                    }
                    
                    // 3. Notifications: Delete all for this user
                    getNotificationsCollection().whereEqualTo("userId", volunteerId).get().addOnSuccessListener { notifSnapshot ->
                        notifSnapshot.documents.forEach { notifDoc ->
                            batch.delete(notifDoc.reference)
                        }
                        
                        // 4. User Active Programs: Delete subcollection documents
                        db.collection("users").document(volunteerId).collection("active_programs").get().addOnSuccessListener { progSnapshot ->
                            progSnapshot.documents.forEach { progDoc ->
                                batch.delete(progDoc.reference)
                            }

                            // 5. Core Records: Volunteer profile and User document
                            batch.delete(getVolunteersCollection().document(volunteerId))
                            batch.delete(db.collection("users").document(volunteerId))
                            
                            batch.commit()
                                .addOnSuccessListener { onComplete(true) }
                                .addOnFailureListener { onComplete(false) }
                        }.addOnFailureListener { onComplete(false) }
                    }.addOnFailureListener { onComplete(false) }
                }.addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeVolunteerFromProject(projectId: String, volunteerId: String, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).update("volunteers", FieldValue.arrayRemove(volunteerId))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getVolunteerById(volunteerId: String, onComplete: (Volunteer?) -> Unit) {
        getVolunteersCollection().document(volunteerId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onComplete(mapToVolunteer(doc))
                else onComplete(null)
            }
            .addOnFailureListener { onComplete(null) }
    }

    /**
     * Fetches a volunteer profile in real-time.
     */
    fun getVolunteerProfileFlow(volunteerId: String): Flow<Volunteer?> = callbackFlow {
        val registration = getVolunteersCollection().document(volunteerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.takeIf { it.exists() }?.let { mapToVolunteer(it) })
            }
        awaitClose { registration.remove() }
    }

    /**
     * Achievements & Gamification
     */
    fun getAchievements(): Flow<List<Achievement>> = callbackFlow {
        val reg = getAchievementsCollection().addSnapshotListener { snp, _ ->
            trySend(snp?.documents?.mapNotNull { mapToAchievement(it) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    /**
     * Events (LumiSphere)
     */
    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val registration = getEventsCollection().orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.documents?.mapNotNull { mapToEvent(it) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun addEvent(event: Event, onComplete: (Boolean) -> Unit) {
        getEventsCollection().add(mapFromEvent(event)).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun updateEvent(event: Event, onComplete: (Boolean) -> Unit) {
        if (event.id.isEmpty()) { onComplete(false); return }
        getEventsCollection().document(event.id).set(mapFromEvent(event), SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun deleteEvent(eventId: String, onComplete: (Boolean) -> Unit) {
        getEventsCollection().document(eventId).delete().addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun getUnlockedAchievements(volunteerId: String): Flow<List<Achievement>> = callbackFlow {
        getVolunteersCollection().document(volunteerId).get().addOnSuccessListener { doc ->
            val ids = doc.get("achievements") as? List<String> ?: emptyList()
            if (ids.isEmpty()) { trySend(emptyList()); return@addOnSuccessListener }
            getAchievementsCollection().whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
                .get().addOnSuccessListener { snp ->
                    trySend(snp.documents.mapNotNull { mapToAchievement(it) })
                }
        }
        awaitClose { }
    }

    /**
     * Notifications
     */
    /**
     * Notifications
     */
    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val reg = getNotificationsCollection()
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snp, _ ->
                val list = snp?.documents?.mapNotNull { mapToNotification(it) } ?: emptyList()
                trySend(list.sortedByDescending { it.timestamp })
            }
        awaitClose { reg.remove() }
    }

    /**
     * Fetches all tasks assigned to a specific volunteer across all projects in real-time.
     * Returns a list of Pairs containing the Task and its associated Project ID.
     */
    fun getVolunteerTasks(volunteerId: String): Flow<List<Pair<Task, String>>> = callbackFlow {
        val registration = getProjectsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val projects = snapshot?.documents?.mapNotNull { mapToProjectUi(it) } ?: emptyList()
            val allTasksWithProject = projects.flatMap { project ->
                project.tasks.filter { it.assignedToIds.contains(volunteerId) }
                    .map { it to project.id }
            }
            trySend(allTasksWithProject)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Updates the status of a specific task in a project.
     */
    fun updateTaskStatusInProject(projectId: String, taskId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        getProjectsCollection().document(projectId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                val updatedTasks = tasks.map { 
                    val id = it["id"] as? String ?: ""
                    if (id == taskId) {
                        it.toMutableMap().apply { this["isDone"] = isDone }
                    } else it
                }
                getProjectsCollection().document(projectId).update("tasks", updatedTasks)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            } else onComplete(false)
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        getNotificationsCollection().document(notificationId).update("isRead", true)
    }

    fun addNotification(notification: Notification) {
        getNotificationsCollection().add(mapFromNotification(notification))
    }

    fun notifyUser(userId: String, title: String, message: String, type: String = "INFO") {
        val notification = Notification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        addNotification(notification)
    }

    /**
     * Checks if a user already exists with the given email across all roles.
     * This ensures that we don't create duplicate accounts.
     */
    fun checkUserExistsByEmail(email: String, onComplete: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(!snapshot.isEmpty)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun createVolunteerUser(volunteer: Volunteer, onComplete: (Boolean) -> Unit) {
        val user = User(
            id = volunteer.id,
            name = volunteer.name,
            email = volunteer.email,
            role = UserRole.VOLUNTEER,
            enabled = true
        )
        db.collection("users").document(user.id).set(mapFromUser(user))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun mapToVolunteer(doc: DocumentSnapshot) = Volunteer(
        id = doc.id,
        name = doc.getString("name") ?: "Unnamed",
        email = doc.getString("email") ?: "",
        phoneNumber = doc.getString("phoneNumber") ?: "",
        profileImageUrl = doc.getString("profileImageUrl"),
        status = doc.getString("status") ?: "Pending",
        skills = doc.get("skills") as? List<String> ?: emptyList(),
        motivation = doc.getString("motivation") ?: "",
        appliedDate = (doc.get("appliedDate") as? Number)?.toLong() ?: System.currentTimeMillis(),
        projectIds = doc.get("projectIds") as? List<String> ?: doc.getString("projectId")?.let { listOf(it) } ?: emptyList(),
        points = doc.getLong("points")?.toInt() ?: 0,
        level = doc.getLong("level")?.toInt() ?: 1,
        trophiesCount = doc.getLong("trophiesCount")?.toInt() ?: 0,
        achievements = doc.get("achievements") as? List<String> ?: emptyList(),
        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
        darkModeEnabled = doc.getBoolean("darkModeEnabled") ?: false
    )

    private fun mapFromVolunteer(v: Volunteer) = hashMapOf(
        "name" to v.name,
        "email" to v.email,
        "phoneNumber" to v.phoneNumber,
        "profileImageUrl" to v.profileImageUrl,
        "status" to v.status,
        "skills" to v.skills,
        "motivation" to v.motivation,
        "appliedDate" to v.appliedDate,
        "projectIds" to v.projectIds,
        "points" to v.points,
        "level" to v.level,
        "trophiesCount" to v.trophiesCount,
        "achievements" to v.achievements,
        "notificationsEnabled" to v.notificationsEnabled,
        "darkModeEnabled" to v.darkModeEnabled
    )

    private fun mapToAchievement(doc: DocumentSnapshot) = Achievement(
        id = doc.id,
        title = doc.getString("title") ?: "",
        description = doc.getString("description") ?: "",
        iconUrl = doc.getString("iconUrl"),
        pointsAwarded = doc.getLong("pointsAwarded")?.toInt() ?: 0,
        criteriaType = doc.getString("criteriaType") ?: "",
        criteriaValue = doc.getLong("criteriaValue")?.toInt() ?: 0
    )

    private fun mapToNotification(doc: DocumentSnapshot) = Notification(
        id = doc.id,
        userId = doc.getString("userId") ?: "",
        title = doc.getString("title") ?: "",
        message = doc.getString("message") ?: "",
        type = doc.getString("type") ?: "INFO",
        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
        isRead = doc.getBoolean("isRead") ?: false
    )

    private fun mapFromNotification(n: Notification) = hashMapOf(
        "userId" to n.userId,
        "title" to n.title,
        "message" to n.message,
        "type" to n.type,
        "timestamp" to FieldValue.serverTimestamp(),
        "isRead" to n.isRead
    )

    /**
     * Donations: Unified transaction that updates multiple collections for consistency.
     * 1. Adds donation record to 'lumisphere/donations/items'
     * 2. Increments 'lumisphere/donations/totalAmount'
     * 3. If projectId is provided, increments project 'spent' (raised) amount
     * 4. If donorId is provided, updates donor's 'lastContactDate'
     */
    fun addDonation(
        donorId: String?, 
        amount: Int, 
        method: String, 
        projectId: String? = null,
        details: Map<String, String> = emptyMap(), 
        onComplete: (Boolean) -> Unit
    ) {
        db.runTransaction { transaction ->
            val donationRef = db.collection("lumisphere").document("donations").collection("items").document()
            val summaryRef = db.collection("lumisphere").document("donations")
            
            val donationData = hashMapOf(
                "donorId" to donorId,
                "projectId" to projectId,
                "amount" to amount,
                "method" to method,
                "details" to details,
                "status" to "Successful",
                "timestamp" to FieldValue.serverTimestamp()
            )
            
            // Link project if applicable
            if (projectId != null) {
                val projectRef = getProjectsCollection().document(projectId)
                transaction.update(projectRef, "spent", FieldValue.increment(amount.toLong()))
                transaction.update(projectRef, "lastUpdated", FieldValue.serverTimestamp())
                
                // For backward compatibility with DonorRepository records
                val projectSnap = transaction.get(projectRef)
                if (projectSnap.exists()) {
                    donationData["campaignTitle"] = projectSnap.getString("name") ?: "Campaign"
                }
            }
            
            // Update donor contact date
            if (donorId != null) {
                transaction.update(getDonorsCollection().document(donorId), "lastContactDate", FieldValue.serverTimestamp())
            }
            
            transaction.set(donationRef, donationData)
            transaction.update(summaryRef, "totalAmount", FieldValue.increment(amount.toLong()))
            // Sync with revenue document for consistency across repositories
            transaction.update(db.collection("lumisphere").document("revenue"), "totalAmount", FieldValue.increment(amount.toLong()))
        }.addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener { e ->
            Log.e("FirestoreService", "Donation transaction failed", e)
            onComplete(false)
        }
    }

    /**
     * Users & Profiles
     */
    fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val reg = db.collection("users").document(userId).addSnapshotListener { doc, _ ->
            trySend(doc?.let { if (it.exists()) mapToUser(it) else null })
        }
        awaitClose { reg.remove() }
    }

    fun updateUserProfile(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(user.id).set(mapFromUser(user), SetOptions.merge())
            .addOnSuccessListener { 
                // Also update the team member record if they exist in teams
                val teamUpdates = hashMapOf<String, Any?>(
                    "name" to user.name,
                    "phone" to user.phoneNumber,
                    "bio" to user.bio,
                    "imageUrl" to user.profileImageUrl,
                    "isTwoFactorEnabled" to user.isTwoFactorEnabled
                )
                getTeamsCollection().document(user.id).get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        getTeamsCollection().document(user.id).update(teamUpdates)
                    }
                }
                onComplete(true) 
            }.addOnFailureListener { onComplete(false) }
    }

    fun getTeamMembers(): Flow<List<User>> = callbackFlow {
        val reg = db.collection("users").addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToUser(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = callbackFlow {
        if (ids.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToUser(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    /**
     * Updates the FCM token for a specific user.
     * Used for sending targeted push notifications.
     */
    fun updateFcmToken(userId: String, token: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId).update("fcmToken", token)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // =========================================================================================
    // MAPPING UTILITIES (Encapsulation for Security & Performance)
    // =========================================================================================

    private fun mapToTask(it: Map<String, Any>) = Task(
        id = it["id"] as? String ?: "",
        title = it["title"] as? String ?: "",
        description = it["description"] as? String ?: "",
        assignedToIds = it["assignedToIds"] as? List<String> ?: (it["assignedToId"] as? String)?.let { id -> listOf(id) } ?: emptyList(),
        assignedToNames = it["assignedToNames"] as? List<String> ?: (it["assignedToName"] as? String)?.let { name -> listOf(name) } ?: emptyList(),
        assignedById = it["assignedById"] as? String ?: "",
        assigneeType = try { AssigneeType.valueOf(it["assigneeType"] as? String ?: "TEAM") } catch (e: Exception) { AssigneeType.TEAM },
        deadline = (it["deadline"] as? Number)?.toLong() ?: 0L,
        isDone = it["isDone"] as? Boolean ?: false,
        createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L
    )

    private fun mapToProjectUi(doc: DocumentSnapshot): Project {
        val rawDate = doc.get("lastUpdated")
        val dateDisplay = when (rawDate) {
            is Timestamp -> dateFormatter.format(rawDate.toDate())
            is String -> rawDate
            else -> "Just now"
        }
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        return Project(
            id = doc.id, name = doc.getString("name") ?: "Unnamed", status = doc.getString("status") ?: "Ongoing",
            budget = doc.getLong("budget")?.toInt() ?: 0, spent = doc.getLong("spent")?.toInt() ?: 0,
            progress = doc.getDouble("progress")?.toFloat() ?: 0f, lastUpdated = dateDisplay,
            imageUrl = doc.getString("imageUrl"), description = doc.getString("description") ?: "",
            location = doc.getString("location") ?: "", startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis(),
            tasks = tasks, volunteers = doc.get("volunteers") as? List<String> ?: emptyList(),
            groupLeaderIds = doc.get("groupLeaderIds") as? List<String> ?: emptyList(),
            teamMemberIds = doc.get("teamMemberIds") as? List<String> ?: emptyList(),
            clients = doc.get("clients") as? List<String> ?: emptyList(), category = doc.getString("category") ?: ""
        )
    }

    private fun mapFromProject(p: Project) = hashMapOf(
        "name" to p.name, "status" to p.status, "budget" to p.budget, "spent" to p.spent, "progress" to p.progress,
        "lastUpdated" to FieldValue.serverTimestamp(), "imageUrl" to p.imageUrl, "description" to p.description,
        "location" to p.location, "startDate" to p.startDate, 
        "tasks" to p.tasks.map { mapFromTask(it) },
        "volunteers" to p.volunteers, 
        "groupLeaderIds" to p.groupLeaderIds, 
        "teamMemberIds" to p.teamMemberIds,
        "clients" to p.clients, 
        "category" to p.category
    )

    private fun mapToFreelanceUi(doc: DocumentSnapshot): Freelance {
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        
        return Freelance(
            id = doc.id, imageUrl = doc.getString("imageUrl"), name = doc.getString("name") ?: "Unnamed",
            description = doc.getString("description") ?: "", category = doc.getString("category") ?: "",
            status = doc.getString("status") ?: "Pending", teamIds = doc.get("teamIds") as? List<String> ?: emptyList(),
            clientIds = doc.get("clientIds") as? List<String> ?: emptyList(), 
            tasks = tasks,
            progress = doc.getDouble("progress")?.toFloat() ?: 0f,
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun mapFromFreelance(f: Freelance) = hashMapOf(
        "name" to f.name, "status" to f.status, "imageUrl" to f.imageUrl, "description" to f.description,
        "category" to f.category, "teamIds" to f.teamIds, "clientIds" to f.clientIds, 
        "tasks" to f.tasks.map { mapFromTask(it) },
        "progress" to f.progress,
        "createdAt" to (if (f.createdAt > 0) f.createdAt else FieldValue.serverTimestamp())
    )

    private fun mapFromTask(it: Task) = mapOf(
        "id" to it.id, 
        "title" to it.title, 
        "description" to it.description,
        "assignedToIds" to it.assignedToIds,
        "assignedToNames" to it.assignedToNames,
        "assignedById" to it.assignedById,
        "assigneeType" to it.assigneeType.name,
        "deadline" to it.deadline,
        "isDone" to it.isDone,
        "createdAt" to it.createdAt
    )

    private fun mapToTeam(doc: DocumentSnapshot) = Team(
        id = doc.id, imageUrl = doc.getString("imageUrl"), name = doc.getString("name") ?: "Unnamed",
        email = doc.getString("email") ?: "", phone = doc.getString("phone") ?: "", department = doc.getString("department") ?: "",
        jobtitle = doc.getString("jobtitle") ?: "", gender = doc.getString("gender") ?: "Male",
        bio = doc.getString("bio") ?: "",
        role = com.example.luminarysolutions.ui.auth.safeValueOf(doc.getString("role")), 
        enabled = doc.getBoolean("enabled") ?: true,
        isTwoFactorEnabled = doc.getBoolean("isTwoFactorEnabled") ?: false,
        datejoined = (doc.get("createdAt") as? Timestamp)?.toDate()?.time ?: (doc.get("datejoined") as? Number)?.toLong() ?: System.currentTimeMillis()
    )

    private fun mapFromTeam(t: Team) = hashMapOf(
        "name" to t.name, "email" to t.email, "phone" to t.phone, "department" to t.department, "jobtitle" to t.jobtitle,
        "gender" to t.gender, "bio" to t.bio, "role" to t.role.name, "enabled" to t.enabled, 
        "isTwoFactorEnabled" to t.isTwoFactorEnabled,
        "imageUrl" to t.imageUrl, "createdAt" to FieldValue.serverTimestamp()
    )

    private fun mapToUser(doc: DocumentSnapshot) = User(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", email = doc.getString("email") ?: "",
        phoneNumber = doc.getString("phoneNumber") ?: "",
        bio = doc.getString("bio") ?: "",
        profileImageUrl = doc.getString("profileImageUrl"),
        role = com.example.luminarysolutions.ui.auth.safeValueOf(doc.getString("role")), 
        enabled = doc.getBoolean("enabled") ?: true,
        fcmToken = doc.getString("fcmToken"),
        isTwoFactorEnabled = doc.getBoolean("isTwoFactorEnabled") ?: false
    )

    private fun mapFromUser(u: User) = hashMapOf(
        "name" to u.name,
        "email" to u.email,
        "phoneNumber" to u.phoneNumber,
        "bio" to u.bio,
        "profileImageUrl" to u.profileImageUrl,
        "role" to u.role.name,
        "enabled" to u.enabled,
        "fcmToken" to u.fcmToken,
        "isTwoFactorEnabled" to u.isTwoFactorEnabled
    )

    private fun mapToDocument(doc: DocumentSnapshot) = Document(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", description = doc.getString("description") ?: "",
        category = doc.getString("category") ?: "PDF", uploader = doc.getString("uploader") ?: "System",
        date = doc.getString("date") ?: "Today", size = doc.getString("size") ?: "0 KB", fileUrl = doc.getString("fileUrl"),
        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
    )

    private fun mapFromDocument(d: Document) = hashMapOf(
        "name" to d.name, "description" to d.description, "category" to d.category, "uploader" to d.uploader,
        "date" to d.date, "size" to d.size, "fileUrl" to d.fileUrl, "timestamp" to FieldValue.serverTimestamp()
    )

    private fun mapToDonor(doc: DocumentSnapshot) = Donor(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", type = doc.getString("type") ?: "Donor",
        status = doc.getString("status") ?: "Active", valueOrNote = doc.getString("valueOrNote") ?: "—",
        lastContact = "Last contact: ${doc.get("lastContactDate")?.let { if (it is Timestamp) dateFormatter.format(it.toDate()) else it.toString() } ?: "No contact"}"
    )

    private fun mapFromDonor(d: Donor) = hashMapOf("name" to d.name, "type" to d.type, "status" to d.status, "valueOrNote" to d.valueOrNote, "lastContactDate" to FieldValue.serverTimestamp())

    private fun mapToPartner(doc: DocumentSnapshot) = Partner(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", type = doc.getString("type") ?: "Partner",
        status = doc.getString("status") ?: "Active", valueOrNote = doc.getString("valueOrNote") ?: "—",
        lastContact = "Last contact: ${doc.get("lastContactDate")?.let { if (it is Timestamp) dateFormatter.format(it.toDate()) else it.toString() } ?: "No contact"}"
    )

    private fun mapFromPartner(p: Partner) = hashMapOf("name" to p.name, "type" to p.type, "status" to p.status, "valueOrNote" to p.valueOrNote, "lastContactDate" to FieldValue.serverTimestamp())

    private fun mapToExpense(doc: DocumentSnapshot) = Expense(
        id = doc.id, category = doc.getString("category") ?: "", account = doc.getString("account") ?: "", amount = doc.getLong("amount")?.toInt() ?: 0,
        date = (doc.get("timestamp") as? Timestamp)?.let { dateFormatter.format(it.toDate()) } ?: "Recently",
        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(), projectId = doc.getString("projectId")
    )

    private fun mapFromExpense(e: Expense) = hashMapOf("category" to e.category, "account" to e.account, "amount" to e.amount, "timestamp" to FieldValue.serverTimestamp(), "projectId" to e.projectId)

    private fun mapToApproval(doc: DocumentSnapshot) = Approval(
        id = doc.id, type = doc.getString("type") ?: "Approval", account = doc.getString("account") ?: "", amount = doc.getLong("amount")?.toInt() ?: 0,
        priority = doc.getString("priority") ?: "Medium", date = (doc.get("timestamp") as? Timestamp)?.let { dateFormatter.format(it.toDate()) } ?: "Recently",
        status = doc.getString("status") ?: "Pending", requestedBy = doc.getString("requestedBy") ?: ""
    )

    private fun mapFromApproval(a: Approval) = hashMapOf("type" to a.type, "account" to a.account, "amount" to a.amount, "priority" to a.priority, "status" to a.status, "requestedBy" to a.requestedBy, "timestamp" to FieldValue.serverTimestamp())

    private fun mapToEvent(doc: DocumentSnapshot) = Event(
        id = doc.id,
        title = doc.getString("title") ?: "",
        description = doc.getString("description") ?: "",
        date = (doc.get("date") as? Number)?.toLong() ?: System.currentTimeMillis(),
        location = doc.getString("location") ?: "",
        imageUrl = doc.getString("imageUrl"),
        projectId = doc.getString("projectId"),
        attendees = doc.get("attendees") as? List<String> ?: emptyList(),
        type = doc.getString("type") ?: "General",
        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
    )

    private fun mapFromEvent(e: Event) = hashMapOf(
        "title" to e.title,
        "description" to e.description,
        "date" to e.date,
        "location" to e.location,
        "imageUrl" to e.imageUrl,
        "projectId" to e.projectId,
        "attendees" to e.attendees,
        "type" to e.type,
        "createdAt" to (if (e.createdAt > 0) e.createdAt else FieldValue.serverTimestamp())
    )
}
