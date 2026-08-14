package com.example.luminarysolutions.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.donor.DonorAchievementsScreen
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.example.luminarysolutions.ui.volunteer.VolunteerAchievementsScreen
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

/**
 * A unified entry point for achievements.
 * Automatically detects the user's role and displays the correct achievement screen.
 * Uses AnimatedContent for smooth transitions between states.
 */
@Composable
fun UnifiedAchievementsScreen(
    vm: UnifiedAchievementsViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "AchievementsStateTransition"
        ) { state ->
            when (state) {
                is UnifiedAchievementsUiState.Loading -> {
                    LoadingView()
                }
                is UnifiedAchievementsUiState.Success -> {
                    RoleSpecificView(state.role)
                }
                is UnifiedAchievementsUiState.Error -> {
                    ErrorView(state.message) { vm.retry() }
                }
            }
        }
    }
}

@Composable
private fun RoleSpecificView(role: UserRole) {
    when (role) {
        UserRole.DONOR -> {
            val donorVm: DonorViewModel = hiltViewModel()
            DonorAchievementsScreen(donorVm)
        }
        UserRole.VOLUNTEER -> {
            val volunteerVm: VolunteerViewModel = hiltViewModel()
            VolunteerAchievementsScreen(volunteerVm)
        }
        else -> {
            // Handle other roles or unauthorized roles
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Trophies Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Achievements are currently only available for Donors and Volunteers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Loading your achievements...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
