package com.example.luminarysolutions.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDashboardScreen(
    viewModel: CampaignDashboardViewModel= viewModel(),
    onCampaignSelected: (Campaign) -> Unit
){
    val campaigns by viewModel.campaigns
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Dashboard")}
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                LoadingState(padding)
            }
            campaigns.isEmpty() -> {
                EmptyState(padding)
            }
            else -> CampaignList(
                campaigns = campaigns,
                padding = padding,
                onCampaignClick = onCampaignSelected
            )
        }
    }
}

@Composable
fun LoadingState(padding: PaddingValues){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(padding: PaddingValues){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ){
        Text("No campaigns available yet")
    }
}

@Composable
fun CampaignList(
    campaigns: List<Campaign>,
    padding: PaddingValues,
    onCampaignClick: (Campaign) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        items(campaigns){ campaign ->
            CampaignCard(
                campaign = campaign,
                onClick = {onCampaignClick(campaign)}
            )
        }
    }
}
@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = campaign.progress,
        animationSpec = tween(
            durationMillis = 800
        ),
        label = " Campaign Progress Animation"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ){
        Column{

            Image(
                painter = painterResource(id = campaign.imageRes),
                contentDescription = campaign.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Column(modifier=Modifier.padding(16.dp)) {
                Text(
                    campaign.title,
                    style = MaterialTheme.typography.titleMedium ,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    campaign.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Raised ${campaign.amountRaised} of ${campaign.goalAmount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
