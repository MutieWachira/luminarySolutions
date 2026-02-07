package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {

    // UI-only sample metrics
    val metrics = remember {
        CommunityMetricsUi(
            totalBeneficiaries = 1280,
            activePrograms = 6,
            openCases = 14,
            outcomeScore = 78
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community & Programs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Text("LumiSphere Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Track beneficiaries, programs, and community feedback.", style = MaterialTheme.typography.bodyMedium)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Beneficiaries", metrics.totalBeneficiaries.toString(), Icons.Default.Groups, Modifier.weight(1f))
                    MetricCard("Programs", metrics.activePrograms.toString(), Icons.Default.VolunteerActivism, Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Open Cases", metrics.openCases.toString(), Icons.Default.ReportProblem, Modifier.weight(1f))
                    MetricCard("Outcome Score", "${metrics.outcomeScore}%", Icons.Default.Assessment, Modifier.weight(1f))
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = { navController.navigate(Screen.Beneficiaries.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Beneficiaries") }

                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Grievances.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Feedback & Grievances") }

                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Outcomes.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Outcome Tracking") }
                    }
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Impact Snapshot (UI-only)", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        val note = "• Completion: ${(metrics.outcomeScore * 0.9f).roundToInt()}%\n" +
                                "• Program reach growing steadily\n" +
                                "• Open cases need follow-up"
                        Text(note)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class CommunityMetricsUi(
    val totalBeneficiaries: Int,
    val activePrograms: Int,
    val openCases: Int,
    val outcomeScore: Int
)
