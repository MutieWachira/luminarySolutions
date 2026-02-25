package com.example.luminarysolutions.ui.volunteer.data

import androidx.compose.runtime.mutableStateListOf
import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.models.VolunteerEventUi
import com.example.luminarysolutions.ui.volunteer.models.VolunteerTaskUi
import java.util.UUID

class InMemoryVolunteerRepository : VolunteerRepository {

    private val tasks = mutableStateListOf(
        VolunteerTaskUi(
            id = UUID.randomUUID().toString(),
            title = "Collect beneficiary forms",
            description = "Visit assigned households and collect completed registration forms.",
            status = TaskStatus.ASSIGNED,
            dueDate = "Today",
            location = "Kibera",
            lastUpdate = "No updates yet"
        ),
        VolunteerTaskUi(
            id = UUID.randomUUID().toString(),
            title = "Upload event attendance",
            description = "Upload attendance list to the system and confirm totals.",
            status = TaskStatus.IN_PROGRESS,
            dueDate = "Tomorrow",
            location = "Mathare",
            lastUpdate = "Started uploading (10 mins ago)"
        ),
        VolunteerTaskUi(
            id = UUID.randomUUID().toString(),
            title = "Submit weekly field report",
            description = "Summarize progress, blockers, and next week plan.",
            status = TaskStatus.DONE,
            dueDate = "Friday",
            location = "HQ",
            lastUpdate = "Submitted report (Yesterday)"
        )
    )

    private val events = mutableStateListOf(
        VolunteerEventUi(
            id = UUID.randomUUID().toString(),
            name = "Community Outreach",
            date = "Sat 10:00",
            venue = "Kawangware",
            notes = "Bring registration forms & flyers."
        ),
        VolunteerEventUi(
            id = UUID.randomUUID().toString(),
            name = "Health Camp",
            date = "Mon 09:00",
            venue = "Kayole",
            notes = "Coordinate with program manager at arrival."
        )
    )

    override fun getMyTasks(userId: String): List<VolunteerTaskUi> = tasks
    override fun getMyEvents(userId: String): List<VolunteerEventUi> = events

    override fun getTask(taskId: String): VolunteerTaskUi? =
        tasks.firstOrNull { it.id == taskId }

    override fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx != -1) {
            val old = tasks[idx]
            tasks[idx] = old.copy(
                status = status,
                lastUpdate = "Status changed to ${status.name} (Just now)"
            )
        }
    }

    override fun addTaskUpdate(taskId: String, note: String) {
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx != -1) {
            val old = tasks[idx]
            tasks[idx] = old.copy(lastUpdate = "$note (Just now)")
        }
    }
}