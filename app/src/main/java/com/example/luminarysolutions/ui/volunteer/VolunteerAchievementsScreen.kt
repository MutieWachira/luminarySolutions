package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel

@Composable
fun VolunteerAchievementsScreen(vm: VolunteerViewModel) {
    val profile by vm.profile.collectAsState()
    val allAchievements by vm.achievements.collectAsState()
    val unlockedAchievements by vm.unlockedAchievements.collectAsState()
    
    // Ensure data is loaded
    LaunchedEffect(Unit) {
        vm.load("me")
    }

    VolunteerAchievementsContent(
        profile = profile,
        allAchievements = allAchievements,
        unlockedAchievements = unlockedAchievements,
        onRefresh = { vm.load("me") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerAchievementsContent(
    profile: Volunteer?,
    allAchievements: List<Achievement>,
    unlockedAchievements: List<Achievement>,
    onRefresh: () -> Unit
) {
    val bronzeCount = unlockedAchievements.count { it.iconUrl?.lowercase() == "bronze" }
    val silverCount = unlockedAchievements.count { it.iconUrl?.lowercase() == "silver" }
    val goldCount = unlockedAchievements.count { it.iconUrl?.lowercase() == "gold" }
    val platinumCount = unlockedAchievements.count { it.iconUrl?.lowercase() == "platinum" }
    val totalCount = unlockedAchievements.size

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Trophies", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
                }
            )
        }
    ) { padding ->
        if (allAchievements.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Synchronizing your achievements...", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onRefresh) {
                        Text("Force Sync")
                    }
                }
            }
        } else {
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
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrophyCountItem("Total", totalCount, MaterialTheme.colorScheme.primary, Icons.Default.EmojiEvents)
                            TrophyCountItem("Platinum", platinumCount, Color(0xFFE5E4E2), Icons.Default.EmojiEvents)
                            TrophyCountItem("Gold", goldCount, Color(0xFFFFD700), Icons.Default.EmojiEvents)
                            TrophyCountItem("Silver", silverCount, Color(0xFFC0C0C0), Icons.Default.EmojiEvents)
                            TrophyCountItem("Bronze", bronzeCount, Color(0xFFCD7F32), Icons.Default.EmojiEvents)
                        }

                        Spacer(Modifier.height(20.dp))
                        
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
}

@Composable
fun TrophyCountItem(label: String, count: Int, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ModernAchievementItem(achievement: Achievement, isUnlocked: Boolean) {
    val tierColor = when (achievement.iconUrl?.lowercase()) {
        "bronze" -> Color(0xFFCD7F32)
        "silver" -> Color(0xFFC0C0C0)
        "gold" -> Color(0xFFFFD700)
        "platinum" -> Color(0xFFE5E4E2)
        else -> MaterialTheme.colorScheme.primary
    }

    val tierLabel = achievement.iconUrl?.uppercase() ?: "RANK"

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            width = if (isUnlocked) 2.dp else 1.dp,
            color = if (isUnlocked) tierColor else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(
                    if (isUnlocked) {
                        Brush.linearGradient(listOf(tierColor, tierColor.copy(alpha = 0.6f)))
                    } else {
                        Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.1f), Color.LightGray.copy(alpha = 0.1f)))
                    }
                ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    val icon = if (achievement.iconUrl == "platinum") Icons.Default.EmojiEvents else Icons.Default.MilitaryTech
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Surface(
                color = if (isUnlocked) tierColor.copy(alpha = 0.1f) else Color.Transparent,
                shape = CircleShape
            ) {
                Text(
                    text = tierLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Black,
                    color = if (isUnlocked) tierColor else Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${achievement.pointsAwarded} XP",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) tierColor else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray,
                lineHeight = 12.sp
            )

            if (isUnlocked) {
                Spacer(Modifier.height(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VolunteerAchievementsScreenPreview() {
    val sampleAchievements = listOf(
        Achievement("task_1", "Quick Starter", "Complete your first task", "bronze", 50, "TASK_COUNT", 1),
        Achievement("task_5", "High Five", "Complete 5 tasks", "bronze", 50, "TASK_COUNT", 5),
        Achievement("task_10", "Reliable Worker", "Complete 10 tasks", "silver", 150, "TASK_COUNT", 10),
        Achievement("task_20", "Task Enthusiast", "Complete 20 tasks", "silver", 150, "TASK_COUNT", 20),
        Achievement("task_40", "Task Veteran", "Complete 40 tasks", "gold", 500, "TASK_COUNT", 40),
        Achievement("task_80", "Grand Architect", "Complete 80 tasks", "gold", 500, "TASK_COUNT", 80),
        Achievement("task_100", "Century Club", "Complete 100 tasks", "platinum", 1000, "TASK_COUNT", 100)
    )
    
    val sampleUnlocked = listOf(
        sampleAchievements[0], 
        sampleAchievements[1], 
        sampleAchievements[2], 
        sampleAchievements[4],
        sampleAchievements[6]
    )
    
    val sampleProfile = Volunteer(
        id = "me",
        name = "John Doe",
        points = 1250,
        level = 3,
        achievements = sampleUnlocked.map { it.id }
    )

    LuminarySolutionsTheme {
        VolunteerAchievementsContent(
            profile = sampleProfile,
            allAchievements = sampleAchievements,
            unlockedAchievements = sampleUnlocked,
            onRefresh = {}
        )
    }
}
