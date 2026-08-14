package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.luminarysolutions.ui.common.PlayStationAchievementHeader
import com.example.luminarysolutions.ui.common.PlayStationTrophyCard
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

/**
 * Redesigned Volunteer Trophy Room inspired by the PlayStation App.
 * Uses a deep dark theme and vertical list for a premium feel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerAchievementsScreen(vm: VolunteerViewModel) {
    val profile by vm.profile.collectAsState()
    val allAchievements by vm.achievements.collectAsState()
    val unlockedAchievements by vm.unlockedAchievements.collectAsState()

    val trophyCounts = remember(unlockedAchievements) {
        unlockedAchievements.groupBy { it.iconUrl?.lowercase() ?: "bronze" }
            .mapValues { it.value.size }
    }
    
    // Ensure data is loaded
    LaunchedEffect(Unit) {
        vm.load("me")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Trophies", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ) 
                },
                actions = {
                    IconButton(onClick = { vm.load("me") }) { 
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF001F3F)
                )
            )
        }
    ) { padding ->
        if (allAchievements.isEmpty()) {
            LoadingAchievementsPlaceholder(padding)
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // PS Style Header
                PlayStationAchievementHeader(
                    xp = profile?.points ?: 0,
                    level = profile?.level ?: 1,
                    unlockedCount = unlockedAchievements.size,
                    totalCount = allAchievements.size,
                    trophyCounts = trophyCounts
                )

                // Trophy List Section
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Service Milestones", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "${unlockedAchievements.size} / ${allAchievements.size}", 
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    itemsIndexed(allAchievements) { index, achievement ->
                        val isUnlocked = unlockedAchievements.any { it.id == achievement.id }
                        PlayStationTrophyCard(achievement, isUnlocked, index)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAchievementsPlaceholder(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF0072CE))
            Spacer(Modifier.height(16.dp))
            Text(
                "Synchronizing your achievements...", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
