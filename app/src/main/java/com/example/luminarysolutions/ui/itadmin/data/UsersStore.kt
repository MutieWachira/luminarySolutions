package com.example.luminarysolutions.ui.itadmin.data

import androidx.compose.runtime.mutableStateListOf
import com.android.identity.util.UUID
import com.example.luminarysolutions.ui.auth.UserRole

object UsersStore{
    //shared list all users in the system
    val users = mutableStateListOf(
        UserUi(UUID.randomUUID().toString(), "CEO Account", "ceo@luminary.com", UserRole.CEO, true),
        UserUi(UUID.randomUUID().toString(), "IT Admin", "itadmin@luminary.com", UserRole.ADMIN, true),
        UserUi(UUID.randomUUID().toString(), "Volunteer 1", "volunteer1@luminary.com", UserRole.VOLUNTEER, true),
        UserUi(UUID.randomUUID().toString(), "Donor 1", "Donor1@luminary.com", UserRole.DONOR, true),
    )
    fun addUser(user: UserUi){
        users.add(0, user)
    }

    fun updateUser(Updated: UserUi){
        val idx = users.indexOfFirst { it.id == Updated.id }
        if (idx != -1) users[idx] = Updated
    }
    fun toggleEnabled(userId: String){
        val idx = users.indexOfFirst {it.id == userId}
        if (idx != 1) users[idx]= users[idx].copy(enabled = ! users[idx].enabled)
    }
    fun getUserById(userId: String) = users.find { it.id == userId }
}

data class UserUi(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val enabled: Boolean
)
