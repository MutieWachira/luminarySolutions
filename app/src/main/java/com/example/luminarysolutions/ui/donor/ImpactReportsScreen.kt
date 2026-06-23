package com.example.luminarysolutions.ui.donor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.donor.models.ImpactReportUi
import com.example.luminarysolutions.ui.donor.viewmodel.DonorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactReportsScreen(
    navController: NavController,
    vm: DonorViewModel = viewModel(),
    isSubScreen: Boolean = false
) {
    val uiState by vm.uiState.collectAsState()
    val categories = listOf("All", "Education", "Health", "Environment", "Water", "Community")

    if (isSubScreen) {
        ImpactReportsContent(uiState, categories, vm)
    } else {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Impact Reports") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                    
                    // Category Selector
                    ImpactCategorySelector(categories, uiState, vm)
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                ImpactReportsContent(uiState, categories, vm, showSelector = false)
            }
        }
    }
}

@Composable
fun ImpactReportsContent(
    uiState: com.example.luminarysolutions.ui.donor.viewmodel.DonorUiState,
    categories: List<String>,
    vm: DonorViewModel,
    showSelector: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showSelector) {
            ImpactCategorySelector(categories, uiState, vm)
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.reports.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reports found for ${uiState.selectedCategory}.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.reports, key = { it.id }) { r ->
                    ReportCard(r)
                }
            }
        }
    }
}

@Composable
fun ImpactCategorySelector(
    categories: List<String>,
    uiState: com.example.luminarysolutions.ui.donor.viewmodel.DonorUiState,
    vm: DonorViewModel
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = uiState.selectedCategory == category,
                onClick = { vm.setCategory(category) },
                label = { Text(category) },
                shape = CircleShape
            )
        }
    }
}

@Composable
private fun ReportCard(r: ImpactReportUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(r.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        r.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text("Period: ${r.period}", style = MaterialTheme.typography.bodySmall)
            Text("Published: ${r.publishedOn}", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text(r.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
