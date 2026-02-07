package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.luminarysolutions.ui.ceo.data.ProjectsStore
import com.example.luminarysolutions.ui.ceo.data.ProjectUpdateUi
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    navController: NavController,
    projectId: String
) {
    val project = remember(projectId) { ProjectsStore.getProject(projectId) }

    // if project not found, show simple error
    if (project == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Project Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Project not found.")
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Budget", "Updates")

    // ✅ get this project's updates (per-project)
    val updates = remember(projectId) { ProjectsStore.getUpdates(projectId) }

    var showAddUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTab = 2
                    showAddUpdateDialog = true
                }
            ) {
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
            Text(project.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = project.status)
                AssistChip(onClick = { }, label = { Text("Updated: ${project.lastUpdated}") })
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniInfoCard("Budget", "$${project.budget}", Modifier.weight(1f))
                MiniInfoCard("Progress", "${(project.progress * 100).roundToInt()}%", Modifier.weight(1f))
                MiniInfoCard("Health", healthText(project.status), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            Text("Completion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = project.progress, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                "Milestone progress is ${(project.progress * 100).roundToInt()}% complete.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

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

            when (selectedTab) {
                0 -> OverviewTab(project.status, project.budget, project.lastUpdated)
                1 -> BudgetTab(project.budget, project.progress)
                2 -> UpdatesTab(
                    updates = updates,
                    onAddUpdateClick = { showAddUpdateDialog = true }
                )
            }
        }
    }

    if (showAddUpdateDialog) {
        AddUpdateDialog(
            onDismiss = { showAddUpdateDialog = false },
            onSave = { title, note ->
                ProjectsStore.addUpdate(
                    projectId,
                    ProjectUpdateUi(title.trim(), note.trim(), "Just now")
                )
                showAddUpdateDialog = false
            }
        )
    }
}

@Composable
private fun OverviewTab(status: String, budget: Int, lastUpdated: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Status: $status")
            Text("Budget: $$budget")
            Text("Last updated: $lastUpdated")
        }
    }
}

@Composable
private fun BudgetTab(budget: Int, progress: Float) {
    val allocated = budget
    val spent = (budget * progress.coerceIn(0f, 1f)).roundToInt()
    val remaining = (allocated - spent).coerceAtLeast(0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Text("Allocated: $$allocated")
            Text("Spent: $$spent")
            Text("Remaining: $$remaining")

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = if (allocated == 0) 0f else (spent.toFloat() / allocated.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UpdatesTab(
    updates: List<ProjectUpdateUi>,
    onAddUpdateClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Updates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Log field reports, milestones, approvals, issues, and progress notes.")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onAddUpdateClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Update")
                }
            }
        }

        if (updates.isEmpty()) {
            AssistChip(onClick = { }, label = { Text("No updates yet") })
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(updates) { u ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(u.title, fontWeight = FontWeight.SemiBold)
                            Text(u.note, style = MaterialTheme.typography.bodyMedium)
                            Text(u.time, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddUpdateDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Project Update") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank() || note.isBlank()) return@TextButton
                onSave(title, note)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
    AssistChip(onClick = { }, label = { Text(status) })
}

private fun healthText(status: String): String = when (status) {
    "At Risk" -> "Needs Attention"
    "Completed" -> "Healthy"
    else -> "Stable"
}
