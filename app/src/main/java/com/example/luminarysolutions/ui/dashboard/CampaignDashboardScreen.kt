package com.example.luminarysolutions.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luminarysolutions.R
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDashboardScreen(
    viewModel: CampaignDashboardViewModel = viewModel(),
    onCampaignSelected: (String) -> Unit
) {
    val campaigns by viewModel.campaigns
    val isLoading by viewModel.isLoading

    StatelessCampaignDashboardScreen(
        campaigns = campaigns,
        isLoading = isLoading,
        onCampaignSelected = onCampaignSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelessCampaignDashboardScreen(
    campaigns: List<Campaign>,
    isLoading: Boolean,
    onCampaignSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
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
fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text("No campaigns available yet")
    }
}

@Composable
fun CampaignList(
    campaigns: List<Campaign>,
    padding: PaddingValues,
    onCampaignClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(campaigns) { campaign ->
            CampaignCard(
                campaign = campaign,
                onClick = { onCampaignClick(campaign.id) }
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
            .padding(10.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)

    ) {
        Column {

            Image(
                painter = painterResource(id = campaign.imageRes),
                contentDescription = campaign.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))

            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    campaign.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    campaign.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                fun Modifier.customProgressBar(): Modifier =
                    this
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))

                LinearProgressIndicator(
                    progress = animatedProgress,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.customProgressBar()
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

@Preview(showBackground = true)
@Composable
fun CampaignDashboardScreenPreview() {
    LuminarySolutionsTheme {
        StatelessCampaignDashboardScreen(
            campaigns = listOf(
                Campaign(
                    id = "1",
                    title = "School Campaign",
                    description = "A campaign to build a new school.",
                    amountRaised = 5000,
                    goalAmount = 10000,
                    imageRes = R.drawable.schoolcampaign
                ),
                Campaign(
                    id = "2",
                    title = "Medical Campaign",
                    description = "A campaign to provide medical supplies.",
                    amountRaised = 7000,
                    goalAmount = 15000,
                    imageRes = R.drawable.medicalcampaign
                )
            ),
            isLoading = false,
            onCampaignSelected = {}
        )
    }
}
