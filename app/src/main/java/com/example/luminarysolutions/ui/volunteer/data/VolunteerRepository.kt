package com.example.luminarysolutions.ui.volunteer.data

import com.example.luminarysolutions.ui.volunteer.models.TaskStatus
import com.example.luminarysolutions.ui.volunteer.models.VolunteerEventUi
import com.example.luminarysolutions.ui.volunteer.models.VolunteerTaskUi

interface VolunteerRepository {
    fun getMyTasks(userId: String): List<VolunteerTaskUi>
    fun getMyEvents(userId: String): List<VolunteerEventUi>
    fun getTask(taskId: String): VolunteerTaskUi?
    fun updateTaskStatus(taskId: String, status: TaskStatus)
    fun addTaskUpdate(taskId: String, note: String)
}