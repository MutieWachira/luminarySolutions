package com.example.luminarysolutions.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.luminarysolutions.data.models.Achievement

/**
 * Common UI components for Achievement/Trophy screens.
 * Redesigned to follow the high-end PlayStation App aesthetic.
 */

@Composable
fun PlayStationAchievementHeader(
    xp: Int,
    level: Int,
    unlockedCount: Int,
    totalCount: Int,
    trophyCounts: Map<String, Int> = emptyMap()
) {
    val nextLevelXP = (level * 500).coerceAtLeast(500)
    val progress = xp.toFloat() / nextLevelXP

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF001F3F)) // Deep PS Blue
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // PS Level Icon (Star-like)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF0072CE), Color(0xFF003087))))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        level.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Trophy Level",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$xp / $nextLevelXP XP",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF0072CE),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Trophy Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrophySummaryItem("Platinum", trophyCounts["platinum"] ?: 0, Color(0xFFE5E4E2), Icons.Default.WorkspacePremium)
            TrophySummaryItem("Gold", trophyCounts["gold"] ?: 0, Color(0xFFFFD700), Icons.Default.EmojiEvents)
            TrophySummaryItem("Silver", trophyCounts["silver"] ?: 0, Color(0xFFC0C0C0), Icons.Default.Stars)
            TrophySummaryItem("Bronze", trophyCounts["bronze"] ?: 0, Color(0xFFCD7F32), Icons.Default.MilitaryTech)
        }
    }
}

@Composable
private fun TrophySummaryItem(label: String, count: Int, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun PlayStationTrophyCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    index: Int
) {
    val tierColor = when (achievement.iconUrl?.lowercase()) {
        "platinum" -> Color(0xFFE5E4E2)
        "gold" -> Color(0xFFFFD700)
        "silver" -> Color(0xFFC0C0C0)
        "bronze" -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.primary
    }

    val rarityLabel = when {
        achievement.pointsAwarded >= 1000 -> "Ultra Rare"
        achievement.pointsAwarded >= 500 -> "Very Rare"
        achievement.pointsAwarded >= 200 -> "Rare"
        else -> "Common"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trophy Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isUnlocked) tierColor.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (achievement.iconUrl?.lowercase()) {
                        "platinum" -> Icons.Default.WorkspacePremium
                        "gold" -> Icons.Default.EmojiEvents
                        "silver" -> Icons.Default.Stars
                        else -> Icons.Default.MilitaryTech
                    },
                    contentDescription = null,
                    tint = if (isUnlocked) tierColor else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
                if (!isUnlocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    color = if (isUnlocked) Color.Black else Color.Gray,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    achievement.description,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isUnlocked) tierColor.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            rarityLabel,
                            color = if (isUnlocked) tierColor else Color.Gray.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isUnlocked) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "+${achievement.pointsAwarded} XP",
                            color = Color(0xFF0072CE),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(start = 88.dp),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

// Legacy components kept for backward compatibility if needed
@Composable
fun AchievementDashboardHeader(
    title: String,
    xp: Int,
    level: Int,
    roleLabel: String,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(200.dp)
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = primaryColor)
            .background(
                Brush.linearGradient(
                    listOf(primaryColor, secondaryColor)
                )
            )
            .clip(RoundedCornerShape(32.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 120.dp.toPx(),
                center = Offset(size.width * 0.9f, size.height * 0.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = 80.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.8f)
            )
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val nextLevelXP = (level * 500).coerceAtLeast(500)
                    val progress = xp.toFloat() / nextLevelXP
                    
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(85.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 6.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(85.dp),
                        color = Color.White,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            level.toString(), 
                            color = Color.White, 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "LVL", 
                            color = Color.White.copy(alpha = 0.8f), 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(24.dp))

                Column {
                    Text(
                        title, 
                        color = Color.White.copy(alpha = 0.8f), 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$xp XP", 
                        color = Color.White, 
                        style = MaterialTheme.typography.displaySmall, 
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            roleLabel, 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernAchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    index: Int
) {
    PlayStationTrophyCard(achievement, isUnlocked, index)
}
