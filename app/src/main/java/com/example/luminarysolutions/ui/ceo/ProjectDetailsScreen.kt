package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.roundToInt
import com.example.luminarysolutions.ui.ceo.models.ProjectUi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    navController: NavController,
    project: ProjectUi
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Budget", "Updates")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* later: edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* later: add update */ }) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Add update")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header block
            Text(project.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = project.status)
                AssistChip(
                    onClick = { },
                    label = { Text("Updated: ${project.lastUpdated}") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // KPI row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniInfoCard("Budget", "$${project.budget}", Modifier.weight(1f))
                MiniInfoCard("Progress", "${(project.progress * 100).roundToInt()}%", Modifier.weight(1f))
                MiniInfoCard("Health", healthText(project.status), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Text("Completion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = project.progress, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                "Milestone progress is ${(project.progress * 100).roundToInt()}% complete.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tab content
            when (selectedTab) {
                0 -> OverviewTab(project)
                1 -> BudgetTab(project)
                2 -> UpdatesTab()
            }
        }
    }
}

@Composable
private fun OverviewTab(project: ProjectUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "This section summarizes the purpose, goals, and current status of the project.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))

            Text("Status: ${project.status}", style = MaterialTheme.typography.bodyMedium)
            Text("Budget: $${project.budget}", style = MaterialTheme.typography.bodyMedium)
            Text("Last updated: ${project.lastUpdated}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BudgetTab(project: ProjectUi) {
    // UI-only mock budget numbers (replace with Firestore later)
    val allocated = project.budget
    val spent = (project.budget * (project.progress.coerceIn(0f, 1f))).roundToInt()
    val remaining = (allocated - spent).coerceAtLeast(0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Text("Allocated: $${allocated}", style = MaterialTheme.typography.bodyMedium)
            Text("Spent: $${spent}", style = MaterialTheme.typography.bodyMedium)
            Text("Remaining: $${remaining}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (spent.toFloat() / allocated.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Text("Spending is ${(spent.toFloat() / allocated * 100).roundToInt()}% of the budget.")
        }
    }
}

@Composable
private fun UpdatesTab() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Text(
                text = "Project updates will appear here (field reports, milestones, approvals).",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            AssistChip(onClick = { }, label = { Text("No updates yet") })
        }
    }
}

@Composable
private fun MiniInfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    AssistChip(
        onClick = { },
        label = { Text(status) }
    )
}

private fun healthText(status: String): String = when (status) {
    "At Risk" -> "Needs Attention"
    "Completed" -> "Healthy"
    else -> "Stable"
}



