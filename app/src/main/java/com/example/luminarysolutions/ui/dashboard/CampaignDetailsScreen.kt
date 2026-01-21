package com.example.luminarysolutions.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailsScreen(
    campaignId: String,
    onBackClick: () -> Unit,
    viewModel: CampaignDetailsViewModel = viewModel()
) {
    LaunchedEffect(campaignId) {
        viewModel.loadCampaign(campaignId)
    }
    val campaign = viewModel.campaign.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campaign Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        campaign?.let {
            CampaignDetailsContent(
                campaign = it,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun CampaignDetailsContent(
    campaign: Campaign,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        CampaignImage()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = campaign.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = campaign.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        DonationProgress(
            raised = campaign.amountRaised,
            goal = campaign.goalAmount
        )

        Spacer(modifier = Modifier.height(24.dp))

        DonateButton()
    }
}

@Composable
fun CampaignImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            "Campaign Image",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DonationProgress(raised: Int, goal: Int) {
    val progress = raised.toFloat() / goal.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "DonationProgress"
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Raised $raised of $goal",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DonateButton() {
    Button(
        onClick = { /* Navigate to donate */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text("Donate Now")
    }
}
