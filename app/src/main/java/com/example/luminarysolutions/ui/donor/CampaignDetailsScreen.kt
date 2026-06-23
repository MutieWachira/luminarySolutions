package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailsScreen(
    navController: NavController,
    campaignId: String,
    vm: DonorViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val campaignFlow = remember(campaignId) { vm.getCampaign(campaignId) }
    val campaign by campaignFlow.collectAsStateWithLifecycle(initialValue = null)
    
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

    BoxWithConstraints {
        val isTablet = maxWidth > 600.dp
        
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
            if (isTablet) {
                // Tablet layout: Side by side
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Left Side: Image and Progress
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CampaignHeroSection(campaign!!, isTablet = true)
                        FundingProgressCard(campaign!!, progress)
                    }
                    
                    // Right Side: Details and Actions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CampaignDescriptionSection(campaign!!)
                        ActionButtons(
                            campaignId = campaign!!.id,
                            uiState = uiState,
                            navController = navController,
                            onJoinClick = { vm.joinCampaign(campaign!!.id) }
                        )
                    }
                }
            } else {
                // Mobile layout: Stacked
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                ) {
                    CampaignHeroSection(campaign!!, isTablet = false)
                    
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        FundingProgressCard(campaign!!, progress)
                        CampaignDescriptionSection(campaign!!)
                        ActionButtons(
                            campaignId = campaign!!.id,
                            uiState = uiState,
                            navController = navController,
                            onJoinClick = { vm.joinCampaign(campaign!!.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignHeroSection(campaign: com.example.luminarysolutions.ui.donor.models.CampaignUi, isTablet: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().height(if (isTablet) 350.dp else 250.dp).clip(RoundedCornerShape(if (isTablet) 24.dp else 0.dp))) {
        AsyncImage(
            model = campaign.imageUrl ?: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c",
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
                    campaign.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                campaign.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun FundingProgressCard(campaign: com.example.luminarysolutions.ui.donor.models.CampaignUi, progress: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Funding Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("KSh ${campaign.goalAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("KSh ${campaign.raisedAmount} raised", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).roundToInt()}% completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun CampaignDescriptionSection(campaign: com.example.luminarysolutions.ui.donor.models.CampaignUi) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            "This campaign is focused on ${campaign.category.lowercase()} initiatives in ${campaign.location}. Your support directly contributes to sustainable development and community empowerment. We ensure transparency by providing regular impact reports and financial breakdowns.",
            lineHeight = 24.sp,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    campaignId: String,
    uiState: com.example.luminarysolutions.ui.donor.viewmodel.DonorUiState,
    navController: NavController,
    onJoinClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Support this cause",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        
        Button(
            onClick = {
                navController.navigate(Screen.Donation.createRoute(campaignId))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.VolunteerActivism, null)
            Spacer(Modifier.width(8.dp))
            Text("Donate Now", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            "Join as a Volunteer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            "Want to make a hands-on impact? Sign up to join our volunteer program for this specific campaign.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.isVolunteeringForCurrentCampaign) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    Text("You are an active volunteer for this project.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        } else if (!uiState.isLoggedIn) {
            OutlinedButton(
                onClick = { navController.navigate(Screen.VolunteerSignUp.createRoute(campaignId)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Groups, null)
                Spacer(Modifier.width(8.dp))
                Text("Sign up to Volunteer", fontWeight = FontWeight.Bold)
            }
        } else if (uiState.isVolunteerRole) {
            Button(
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join Project", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            OutlinedButton(
                onClick = { navController.navigate(Screen.VolunteerSignUp.createRoute(campaignId)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.VolunteerActivism, null)
                Spacer(Modifier.width(8.dp))
                Text("Become a Volunteer", fontWeight = FontWeight.Bold)
            }
        }
    }
}
