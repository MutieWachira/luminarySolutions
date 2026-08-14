package com.example.luminarysolutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

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
            userRepository.getUserProfile(uid).collect { user ->
                _userProfile.value = user
            }
        }
    }

    fun updateFcmToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.updateFcmToken(uid, token)
        }
    }
}
