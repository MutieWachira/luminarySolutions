package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel

/**
 * Redesigned Donor Trophy Room inspired by the PlayStation App.
 * Uses a deep dark theme and vertical list for a premium feel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorAchievementsScreen(vm: DonorViewModel) {
    val uiState by vm.uiState.collectAsState()
    val donor = uiState.donor
    val allAchievements = uiState.allAchievements
    val unlockedAchievements = uiState.unlockedAchievements

    val trophyCounts = remember(unlockedAchievements) {
        unlockedAchievements.groupBy { it.iconUrl?.lowercase() ?: "bronze" }
            .mapValues { it.value.size }
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
                    IconButton(onClick = { uiState.currentUserId?.let { vm.loadUserStats(it) } }) { 
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF001F3F) // Matching Header
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // PS Style Header
            PlayStationAchievementHeader(
                xp = donor?.points ?: 0,
                level = donor?.level ?: 1,
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
                            "All Trophies", 
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
