package com.example.luminarysolutions.ui.ceo.data

import androidx.compose.runtime.mutableStateListOf
import com.example.luminarysolutions.ui.ceo.models.ProjectUi
import java.util.UUID

/**
 * UI-only in-memory store.
 * Later we replace this with Firestore.
 */
object ProjectsStore {

    val projects = mutableStateListOf(
        ProjectUi(UUID.randomUUID().toString(), "Clean Water Initiative", "Ongoing", 120000, 90000, 0.72f, "2 days ago"),
        ProjectUi(UUID.randomUUID().toString(), "Youth Skills Program", "Ongoing", 80000, 70000, 0.45f, "Today"),
        ProjectUi(UUID.randomUUID().toString(), "School Renovation", "Ongoing", 50000, 50000, 1.00f, "1 week ago"),
        ProjectUi(UUID.randomUUID().toString(), "Community Health Outreach", "Ongoing", 200000, 150000, 0.30f, "Yesterday")
    )

    // updates per projectId
    private val updatesMap = mutableMapOf<String, MutableList<ProjectUpdateUi>>()

    fun getProject(projectId: String): ProjectUi? {
        return projects.firstOrNull { it.id == projectId }
    }

    fun getUpdates(projectId: String): MutableList<ProjectUpdateUi> {
        return updatesMap.getOrPut(projectId) {
            mutableListOf(
                ProjectUpdateUi("Kickoff completed", "Project launched and teams assigned.", "Today")
            )
        }
    }

    fun addUpdate(projectId: String, update: ProjectUpdateUi) {
        val list = getUpdates(projectId)
        list.add(0, update)
    }

    fun addProject(project: ProjectUi) {
        projects.add(0, project)
    }
}

data class ProjectUpdateUi(
    val title: String,
    val note: String,
    val time: String
)
