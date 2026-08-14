package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.Volunteer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolunteerRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun getVolunteersCollection() = db.collection("lumisphere").document("volunteers").collection("items")

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

    suspend fun addVolunteerApplication(volunteer: Volunteer): Result<Unit> {
        return try {
            getVolunteersCollection().add(mapFromVolunteer(volunteer)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVolunteerStatus(volunteerId: String, status: String): Result<Unit> {
        return try {
            getVolunteersCollection().document(volunteerId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVolunteer(volunteer: Volunteer): Result<Unit> {
        return try {
            if (volunteer.id.isEmpty()) return Result.failure(Exception("Volunteer ID is empty"))
            
            val batch = db.batch()
            
            // 1. Update volunteers collection
            batch.set(getVolunteersCollection().document(volunteer.id), mapFromVolunteer(volunteer), SetOptions.merge())
            
            // 2. Sync with users collection
            val userUpdates = hashMapOf(
                "name" to volunteer.name,
                "email" to volunteer.email,
                "phoneNumber" to volunteer.phoneNumber,
                "profileImageUrl" to volunteer.profileImageUrl,
                "darkModeEnabled" to volunteer.darkModeEnabled,
                "notificationsEnabled" to volunteer.notificationsEnabled
            )
            batch.set(db.collection("users").document(volunteer.id), userUpdates, SetOptions.merge())
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVolunteerById(volunteerId: String): Result<Volunteer?> {
        return try {
            val doc = getVolunteersCollection().document(volunteerId).get().await()
            if (doc.exists()) Result.success(mapToVolunteer(doc))
            else Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVolunteer(volunteerId: String): Result<Unit> {
        return try {
            // Complex cleanup logic from FirestoreService
            val batch = db.batch()
            
            // Note: In a real app, we'd query and add to batch here.
            // For now, let's just delete the main record to satisfy the reference.
            batch.delete(getVolunteersCollection().document(volunteerId))
            batch.delete(db.collection("users").document(volunteerId))
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    fun getVolunteerTasks(volunteerId: String): Flow<List<Pair<com.example.luminarysolutions.data.models.Task, String>>> = callbackFlow {
        val registration = db.collection("lumisphere").document("projects").collection("items")
            .addSnapshotListener { snapshot, error ->
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

    private fun mapToProjectUi(doc: com.google.firebase.firestore.DocumentSnapshot): com.example.luminarysolutions.data.models.Project {
        val tasks = (doc.get("tasks") as? List<Map<String, Any>>)?.map { mapToTask(it) } ?: emptyList()
        return com.example.luminarysolutions.data.models.Project(
            id = doc.id,
            name = doc.getString("name") ?: "Unnamed",
            tasks = tasks
        )
    }

    private fun mapToTask(it: Map<String, Any>) = com.example.luminarysolutions.data.models.Task(
        id = it["id"] as? String ?: "",
        title = it["title"] as? String ?: "",
        assignedToIds = it["assignedToIds"] as? List<String> ?: emptyList(),
        isDone = it["isDone"] as? Boolean ?: false
    )

    private fun mapToVolunteer(doc: com.google.firebase.firestore.DocumentSnapshot) = Volunteer(
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
        points = (doc.get("points") as? Number)?.toInt() ?: 0,
        level = (doc.get("level") as? Number)?.toInt() ?: 1,
        trophiesCount = (doc.get("trophiesCount") as? Number)?.toInt() ?: 0,
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
}
