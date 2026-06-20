package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerAchievementsScreen(vm: VolunteerViewModel) {
    val profile by vm.profile.collectAsState()
    val allAchievements by vm.achievements.collectAsState()
    val unlockedAchievements by vm.unlockedAchievements.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Trophies", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { /* Help */ }) { Icon(Icons.Default.HelpOutline, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()
        ) {
            // Level Progress Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Level ${profile?.level ?: 1}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${profile?.points ?: 0} Total XP", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val nextLevelPoints = ((profile?.level ?: 1) * 500).coerceAtLeast(500)
                    val progress = (profile?.points ?: 0).toFloat() / nextLevelPoints
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${nextLevelPoints - (profile?.points ?: 0)} XP to next level",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Achievements", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("${unlockedAchievements.size}/${allAchievements.size} Unlocked", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allAchievements) { achievement ->
                    val isUnlocked = unlockedAchievements.any { it.id == achievement.id }
                    ModernAchievementItem(achievement, isUnlocked)
                }
            }
        }
    }
}

@Composable
fun ModernAchievementItem(achievement: Achievement, isUnlocked: Boolean) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder(isUnlocked)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(
                    if (isUnlocked) {
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                    } else {
                        Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = 0.2f)))
                    }
                ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                minLines = 1,
                maxLines = 1
            )
            Text(
                text = "${achievement.pointsAwarded} XP",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            if (isUnlocked) {
                Spacer(Modifier.height(4.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = Color.Gray
                )
            }
        }
    }
}
