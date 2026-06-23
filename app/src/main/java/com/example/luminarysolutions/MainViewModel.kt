package com.example.luminarysolutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.data.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreService = FirestoreService

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                observeUserProfile(uid)
            } else {
                _userProfile.value = null
            }
        }
    }

    private fun observeUserProfile(uid: String) {
        viewModelScope.launch {
            firestoreService.getUserProfile(uid).collect { user ->
                _userProfile.value = user
            }
        }
    }
}
