package com.example.luminarysolutions.ui.volunteer.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.luminarysolutions.ui.volunteer.data.InMemoryVolunteerRepository
import com.example.luminarysolutions.ui.volunteer.data.VolunteerRepository
import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.models.VolunteerEventUi
import com.example.luminarysolutions.ui.volunteer.models.VolunteerTaskUi

class VolunteerViewModel(
    private val repo: VolunteerRepository = InMemoryVolunteerRepository()
) : ViewModel() {

    var tasks by mutableStateOf<List<VolunteerTaskUi>>(emptyList())
        private set

    var events by mutableStateOf<List<VolunteerEventUi>>(emptyList())
        private set

    fun load(userId: String) {
        tasks = repo.getMyTasks(userId)
        events = repo.getMyEvents(userId)
    }

    fun getTask(taskId: String): VolunteerTaskUi? = repo.getTask(taskId)

    fun setStatus(taskId: String, status: TaskStatus) {
        repo.updateTaskStatus(taskId, status)
        tasks = repo.getMyTasks("me")
    }

    fun addUpdate(taskId: String, note: String) {
        repo.addTaskUpdate(taskId, note)
        tasks = repo.getMyTasks("me")
    }
}