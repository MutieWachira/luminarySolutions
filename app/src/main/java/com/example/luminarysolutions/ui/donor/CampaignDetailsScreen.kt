package com.example.luminarysolutions.ui.donor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailsScreen(
    navController: NavController,
    campaignId: String,
    vm: DonorViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val campaignFlow = remember(campaignId) { vm.getCampaign(campaignId) }
    val campaign by campaignFlow.collectAsState(initial = null)
    
    val scrollState = rememberScrollState()

    // Status Check
    LaunchedEffect(campaignId, uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            vm.checkVolunteerStatusForCampaign(campaignId)
        }
    }

    if (campaign == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) CircularProgressIndicator()
            else Text("Campaign details not available.")
        }
        return
    }

    val progress = if (campaign!!.goalAmount == 0) 0f else (campaign!!.raisedAmount.toFloat() / campaign!!.goalAmount).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Hero Image with Gradient Overlay
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                AsyncImage(
                    model = campaign!!.imageUrl ?: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 300f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            campaign!!.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        campaign!!.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress Bar
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Funding Goal", fontWeight = FontWeight.Bold)
                            Text("KSh ${campaign!!.goalAmount}", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("KSh ${campaign!!.raisedAmount} raised", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("${(progress * 100).roundToInt()}% completed", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                // Description
                Text(
                    "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "This campaign is focused on ${campaign!!.category.lowercase()} initiatives in ${campaign!!.location}. Your support directly contributes to sustainable development and community empowerment. We ensure transparency by providing regular impact reports and financial breakdowns.",
                    lineHeight = 24.sp,
                    color = Color.DarkGray
                )

                // Donation Section
                Text(
                    "Support this cause",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                
                Button(
                    onClick = {
                        navController.navigate(Screen.Donation.createRoute(campaign!!.id))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = true
                ) {
                    Icon(Icons.Default.VolunteerActivism, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Donate Now", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Volunteer Section
                Text(
                    "Join as a Volunteer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Want to make a hands-on impact? Sign up to join our volunteer program for this specific campaign.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                if (uiState.isVolunteeringForCurrentCampaign) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                            Text("You are an active volunteer for this project.", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                } else if (!uiState.isLoggedIn) {
                    // Not logged in: Redirect to Sign Up
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.VolunteerSignUp.createRoute(campaign!!.id)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Groups, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign up to Volunteer", fontWeight = FontWeight.Bold)
                    }
                } else if (uiState.isVolunteerRole) {
                    // Logged in as Volunteer but not in THIS project
                    Button(
                        onClick = { vm.joinCampaign(campaign!!.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Join Project", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Logged in but not a volunteer role (e.g. Donor)
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.VolunteerSignUp.createRoute(campaign!!.id)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Become a Volunteer", fontWeight = FontWeight.Bold)
                    }
                }

                // Alert for UI Messages
                uiState.uiMessage?.let { message ->
                    Snackbar {
                        Text(message)
                    }
                }
            }
        }
    }
}
