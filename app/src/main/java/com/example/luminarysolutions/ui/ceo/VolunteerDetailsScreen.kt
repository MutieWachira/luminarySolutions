package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Achievement
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetailsScreen(
    volunteerId: String,
    navController: NavController,
    viewModel: VolunteerDetailsViewModel = VolunteerDetailsViewModel(volunteerId)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else if (uiState.volunteer != null) {
            val volunteer = uiState.volunteer!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF8F9FA))
            ) {
                VolunteerHeader(volunteer)
                
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    VolunteerInfoSection(volunteer)
                    CampaignsSection(uiState.volunteeredCampaigns)
                    AchievementsSection(uiState.unlockedAchievements, volunteer.trophiesCount)
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Volunteer not found", color = Color.Gray)
            }
        }
    }
}

@Composable
fun VolunteerHeader(volunteer: Volunteer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF111827), Color(0xFF1F2937))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(100.dp),
                border = BorderStroke(4.dp, Color.White.copy(alpha = 0.1f)),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                AsyncImage(
                    model = volunteer.profileImageUrl ?: "https://ui-avatars.com/api/?name=${volunteer.name}&background=10B981&color=fff",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(volunteer.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (volunteer.status == "Approved") Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    volunteer.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VolunteerInfoSection(volunteer: Volunteer) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("General Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            VolunteerInfoRow(Icons.Default.Email, "Email", volunteer.email)
            VolunteerInfoRow(Icons.Default.Phone, "Phone", volunteer.phoneNumber.ifBlank { "Not provided" })
            VolunteerInfoRow(Icons.Default.Star, "Skills", volunteer.skills.joinToString(", ").ifBlank { "General Support" })
            
            Divider(color = Color(0xFFF1F5F9))
            
            Text("Motivation", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(
                volunteer.motivation.ifBlank { "No motivation statement provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
            
            Divider(color = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Impact Points", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(volunteer.points.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Level", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(volunteer.level.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF6366F1))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Trophies", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(volunteer.trophiesCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
fun VolunteerInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CampaignsSection(campaigns: List<Project>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Volunteered Campaigns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (campaigns.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Hasn't volunteered for any campaigns yet.",
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            campaigns.forEach { project ->
                CampaignMiniCard(project)
            }
        }
    }
}

@Composable
fun CampaignMiniCard(project: Project) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(56.dp), color = Color(0xFFF8F9FA)) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(project.location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.1f)) {
                Text(
                    "COMPLETED",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AchievementsSection(achievements: List<Achievement>, trophiesCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Achievements & Trophies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${achievements.size} Unlocked", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
        }
        
        if (achievements.isEmpty() && trophiesCount == 0) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No achievements unlocked yet.",
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trophies are represented by a special card
                repeat(trophiesCount) { index ->
                    TrophyCard(index)
                }
                
                achievements.forEach { achievement ->
                    AchievementBadge(achievement)
                }
            }
        }
    }
}

@Composable
fun TrophyCard(index: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.dp, Color(0xFFFED7AA)),
        modifier = Modifier.size(100.dp, 120.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("Trophy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("#${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementBadge(achievement: Achievement) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.size(100.dp, 120.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = Color(0xFF6366F1).copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MilitaryTech, null, tint = Color(0xFF6366F1))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(achievement.title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Text("${achievement.pointsAwarded} pts", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
