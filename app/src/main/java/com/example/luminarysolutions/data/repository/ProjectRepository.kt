package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.AssigneeType
import com.example.luminarysolutions.data.models.Document
import com.example.luminarysolutions.data.models.Event
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Partner
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
class ProjectRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    // Collections
    private fun getLuminaryProjectsCollection() = db.collection("luminary").document("freelances").collection("items")
    private fun getEnquiriesCollection() = db.collection("luminary").document("enquiries").collection("items")
    private fun getAcquisitionsCollection() = db.collection("luminary").document("acquisitions").collection("items")
    private fun getProjectsCollection() = db.collection("lumisphere").document("projects").collection("items")
    private fun getPartnersCollection() = db.collection("lumisphere").document("partners").collection("items")
    private fun getEventsCollection() = db.collection("lumisphere").document("events").collection("items")
    private fun getDocumentsCollection() = db.collection("luminary").document("documents").collection("items")

    fun getPartners(): Flow<List<Partner>> = callbackFlow {
        val reg = getPartnersCollection().orderBy("lastContactDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToPartner(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val reg = getEventsCollection().orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToEvent(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    fun getDocuments(): Flow<List<Document>> = callbackFlow {
        val reg = getDocumentsCollection().orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToDocument(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    /**
     * LumiSphere Projects
     */
    fun getProjects(): Flow<List<Project>> = callbackFlow {
        val reg = getProjectsCollection().orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snp, _ -> trySend(snp?.documents?.mapNotNull { mapToProjectUi(it) } ?: emptyList()) }
        awaitClose { reg.remove() }
    }

    private fun mapToPartner(doc: DocumentSnapshot) = Partner(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", type = doc.getString("type") ?: "Partner",
        status = doc.getString("status") ?: "Active", valueOrNote = doc.getString("valueOrNote") ?: "—",
        lastContact = "Last contact: ${doc.get("lastContactDate")?.let { if (it is Timestamp) dateFormatter.format(it.toDate()) else it.toString() } ?: "No contact"}"
    )

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

    private fun mapToDocument(doc: DocumentSnapshot) = Document(
        id = doc.id, name = doc.getString("name") ?: "Unnamed", description = doc.getString("description") ?: "",
        category = doc.getString("category") ?: "PDF", uploader = doc.getString("uploader") ?: "System",
        date = doc.getString("date") ?: "Today", size = doc.getString("size") ?: "0 KB", fileUrl = doc.getString("fileUrl"),
        timestamp = (doc.get("timestamp") as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
    )

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

    suspend fun addLuminaryProject(freelance: Freelance): Result<Unit> {
        return try {
            getLuminaryProjectsCollection().add(mapFromFreelance(freelance)).await()
            db.collection("luminary").document("freelances").set(
                mapOf("count" to FieldValue.increment(1)),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLuminaryProject(freelance: Freelance): Result<Unit> {
        return try {
            if (freelance.id.isEmpty()) return Result.failure(Exception("Project ID is empty"))
            getLuminaryProjectsCollection().document(freelance.id).update(mapFromFreelance(freelance) as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLuminaryProject(projectId: String): Result<Unit> {
        return try {
            getLuminaryProjectsCollection().document(projectId).delete().await()
            db.collection("luminary").document("freelances").update("count", FieldValue.increment(-1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFreelanceTasks(freelanceId: String, tasks: List<Task>): Result<Unit> {
        return try {
            val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
            getLuminaryProjectsCollection().document(freelanceId).update(
                mapOf(
                    "tasks" to tasks.map { mapFromTask(it) },
                    "progress" to progress,
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
        val registration = getProjectsCollection().document(projectId).addSnapshotListener { doc, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                trySend(mapToProjectUi(doc))
            } else {
                // If not in projects (LumiSphere), check freelances (Luminary)
                getLuminaryProjectsCollection().document(projectId).get().addOnSuccessListener { lumDoc ->
                    if (lumDoc.exists()) {
                        trySend(mapFreelanceToProject(lumDoc))
                    } else {
                        trySend(null)
                    }
                }.addOnFailureListener { trySend(null) }
            }
        }
        awaitClose { registration.remove() }
    }

    private fun mapFreelanceToProject(doc: DocumentSnapshot): Project {
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        return Project(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed",
            status = doc.getString("status") ?: "Ongoing",
            imageUrl = doc.getString("imageUrl"),
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "Freelance",
            tasks = tasks,
            teamMemberIds = doc.get("teamIds") as? List<String> ?: emptyList(),
            clients = doc.get("clientIds") as? List<String> ?: emptyList(),
            progress = doc.getDouble("progress")?.toFloat() ?: 0f,
            startDate = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    suspend fun updateProjectTeamMembers(projectId: String, teamMemberIds: List<String>): Result<Unit> {
        return try {
            getProjectsCollection().document(projectId).update(
                mapOf(
                    "teamMemberIds" to teamMemberIds,
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignGroupLeader(projectId: String, leaderId: String): Result<Unit> {
        return try {
            getProjectsCollection().document(projectId).update(
                mapOf(
                    "groupLeaderId" to leaderId,
                    "groupLeaderIds" to FieldValue.arrayUnion(leaderId),
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeVolunteerFromProject(projectId: String, volunteerId: String): Result<Unit> {
        return try {
            getProjectsCollection().document(projectId).update("volunteers", FieldValue.arrayRemove(volunteerId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTasksToProject(projectId: String, newTasks: List<Task>): Result<Unit> {
        return try {
            val doc = getProjectsCollection().document(projectId).get().await()
            if (doc.exists()) {
                val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                newTasks.forEach { tasks.add(mapFromTask(it)) }
                updateProjectTasksInternal(projectId, tasks.map { mapToTask(it) }, false)
            } else {
                val lumDoc = getLuminaryProjectsCollection().document(projectId).get().await()
                if (lumDoc.exists()) {
                    val tasks = (lumDoc.get("tasks") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                    newTasks.forEach { tasks.add(mapFromTask(it)) }
                    updateProjectTasksInternal(projectId, tasks.map { mapToTask(it) }, true)
                } else Result.failure(Exception("Project not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateProjectTasksInternal(projectId: String, tasks: List<Task>, isLuminary: Boolean): Result<Unit> {
        return try {
            val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
            val collection = if (isLuminary) getLuminaryProjectsCollection() else getProjectsCollection()
            collection.document(projectId).update(
                mapOf(
                    "tasks" to tasks.map { mapFromTask(it) },
                    "progress" to progress,
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    fun getAssignedProjects(userIds: List<String>): Flow<List<Project>> = callbackFlow {
        val registration = getProjectsCollection()
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

    suspend fun addProject(project: Project): Result<Unit> {
        return try {
            getProjectsCollection().add(mapFromProject(project)).await()
            db.collection("lumisphere").document("projects").update("count", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            // If projects doc doesn't exist, create it
            try {
                db.collection("lumisphere").document("projects").set(mapOf("count" to 1), SetOptions.merge()).await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    suspend fun updateProject(project: Project): Result<Unit> {
        return try {
            if (project.id.isEmpty()) return Result.failure(Exception("Project ID is empty"))
            getProjectsCollection().document(project.id).update(
                mapFromProject(project).apply {
                    this["lastUpdated"] = FieldValue.serverTimestamp()
                } as Map<String, Any>
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            getProjectsCollection().document(projectId).delete().await()
            db.collection("lumisphere").document("projects").update("count", FieldValue.increment(-1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPartner(partner: Partner): Result<Unit> {
        return try {
            getPartnersCollection().add(mapFromPartner(partner)).await()
            db.collection("lumisphere").document("partners").update("count", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addEvent(event: Event): Result<Unit> {
        return try {
            getEventsCollection().add(mapFromEvent(event)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            if (event.id.isEmpty()) return Result.failure(Exception("Event ID is empty"))
            getEventsCollection().document(event.id).set(mapFromEvent(event), SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            getEventsCollection().document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDocument(document: Document): Result<Unit> {
        return try {
            getDocumentsCollection().add(mapFromDocument(document)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(docId: String): Result<Unit> {
        return try {
            getDocumentsCollection().document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Enquiries and Service Acquisitions
     */
    suspend fun sendEnquiry(enquiry: com.example.luminarysolutions.data.models.Enquiry): Result<Unit> {
        return try {
            getEnquiriesCollection().add(mapFromEnquiry(enquiry)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acquireService(acquisition: com.example.luminarysolutions.data.models.ServiceAcquisition): Result<Unit> {
        return try {
            getAcquisitionsCollection().add(mapFromAcquisition(acquisition)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(projectId: String, taskId: String, isDone: Boolean, isFreelance: Boolean): Result<Unit> {
        return try {
            val collection = if (isFreelance) getLuminaryProjectsCollection() else getProjectsCollection()
            val doc = collection.document(projectId).get().await()
            
            if (doc.exists()) {
                val tasksMap = doc.get("tasks") as? List<Map<String, Any>> ?: emptyList()
                val updatedTasks = tasksMap.map { 
                    if (it["id"] == taskId) {
                        it.toMutableMap().apply { this["isDone"] = isDone }
                    } else it
                }
                
                val progress = if (updatedTasks.isEmpty()) 0f else updatedTasks.count { it["isDone"] as? Boolean == true }.toFloat() / updatedTasks.size
                
                collection.document(projectId).update(
                    mapOf(
                        "tasks" to updatedTasks,
                        "progress" to progress,
                        "lastUpdated" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Project not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mapping Helpers (extracted from FirestoreService)
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
        val dateDisplay = when (val rawDate = doc.get("lastUpdated")) {
            is Timestamp -> dateFormatter.format(rawDate.toDate())
            is String -> rawDate
            else -> "Just now"
        }
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        return Project(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed",
            status = doc.getString("status") ?: "Ongoing",
            budget = (doc.get("budget") as? Number)?.toInt() ?: 0,
            spent = (doc.get("spent") as? Number)?.toInt() ?: 0,
            progress = (doc.get("progress") as? Number)?.toFloat() ?: 0f,
            lastUpdated = dateDisplay,
            imageUrl = doc.getString("imageUrl"),
            description = doc.getString("description") ?: "",
            location = doc.getString("location") ?: "",
            startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis(),
            tasks = tasks,
            volunteers = doc.get("volunteers") as? List<String> ?: emptyList(),
            groupLeaderIds = doc.get("groupLeaderIds") as? List<String> ?: emptyList(),
            teamMemberIds = doc.get("teamMemberIds") as? List<String> ?: emptyList(),
            clients = doc.get("clients") as? List<String> ?: emptyList(),
            category = doc.getString("category") ?: ""
        )
    }

    private fun mapToFreelanceUi(doc: DocumentSnapshot): Freelance {
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        return Freelance(
            id = doc.id,
            imageUrl = doc.getString("imageUrl"),
            name = doc.getString("name") ?: "Unnamed",
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "",
            status = doc.getString("status") ?: "Pending",
            price = doc.getString("price") ?: "",
            duration = doc.getString("duration") ?: "",
            benefits = doc.get("benefits") as? List<String> ?: emptyList(),
            processSteps = doc.get("processSteps") as? List<String> ?: emptyList(),
            rating = (doc.get("rating") as? Number)?.toFloat() ?: 5.0f,
            reviewsCount = (doc.get("reviewsCount") as? Number)?.toInt() ?: 0,
            teamIds = doc.get("teamIds") as? List<String> ?: emptyList(),
            clientIds = doc.get("clientIds") as? List<String> ?: emptyList(),
            tasks = tasks,
            progress = (doc.get("progress") as? Number)?.toFloat() ?: 0f,
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
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

    private fun mapFromPartner(p: Partner) = hashMapOf(
        "name" to p.name, "type" to p.type, "status" to p.status, "valueOrNote" to p.valueOrNote, "lastContactDate" to FieldValue.serverTimestamp()
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

    private fun mapFromDocument(d: Document) = hashMapOf(
        "name" to d.name, "description" to d.description, "category" to d.category, "uploader" to d.uploader,
        "date" to d.date, "size" to d.size, "fileUrl" to d.fileUrl, "timestamp" to FieldValue.serverTimestamp()
    )

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

    private fun mapFromEnquiry(e: com.example.luminarysolutions.data.models.Enquiry) = hashMapOf(
        "serviceId" to e.serviceId,
        "serviceName" to e.serviceName,
        "clientId" to e.clientId,
        "clientName" to e.clientName,
        "subject" to e.subject,
        "message" to e.message,
        "status" to e.status,
        "createdAt" to FieldValue.serverTimestamp()
    )

    private fun mapFromAcquisition(a: com.example.luminarysolutions.data.models.ServiceAcquisition) = hashMapOf(
        "serviceId" to a.serviceId,
        "serviceName" to a.serviceName,
        "clientId" to a.clientId,
        "clientName" to a.clientName,
        "price" to a.price,
        "status" to a.status,
        "createdAt" to FieldValue.serverTimestamp()
    )
}
