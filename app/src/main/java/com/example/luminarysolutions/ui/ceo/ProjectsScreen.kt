package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(navController: NavController) {

    // ✅ Make projects mutable so we can add new ones
    val projects = remember {
        mutableStateListOf(
            ProjectUi("Clean Water Initiative", "Ongoing", 120000, 0.72f, "2 days ago"),
            ProjectUi("Youth Skills Program", "At Risk", 80000, 0.45f, "Today"),
            ProjectUi("School Renovation", "Completed", 50000, 1.00f, "1 week ago"),
            ProjectUi("Community Health Outreach", "Ongoing", 200000, 0.30f, "Yesterday")
        )
    }

    var selectedFilter by remember { mutableStateOf(ProjectFilter.ALL) }

    // ✅ Dialog state
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredProjects = remember(selectedFilter, projects) {
        when (selectedFilter) {
            ProjectFilter.ALL -> projects
            ProjectFilter.ONGOING -> projects.filter { it.status == "Ongoing" }
            ProjectFilter.COMPLETED -> projects.filter { it.status == "Completed" }
            ProjectFilter.AT_RISK -> projects.filter { it.status == "At Risk" }
        }
    }

    // KPI counts
    val total = projects.size
    val ongoing = projects.count { it.status == "Ongoing" }
    val completed = projects.count { it.status == "Completed" }
    val atRisk = projects.count { it.status == "At Risk" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects & Operations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* later: search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add project")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ✅ KPI Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniKpiCard(title = "Total", value = total.toString(), modifier = Modifier.weight(1f))
                MiniKpiCard(title = "Ongoing", value = ongoing.toString(), modifier = Modifier.weight(1f))
                MiniKpiCard(title = "Completed", value = completed.toString(), modifier = Modifier.weight(1f))
                MiniKpiCard(title = "At Risk", value = atRisk.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == ProjectFilter.ALL,
                    onClick = { selectedFilter = ProjectFilter.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == ProjectFilter.ONGOING,
                    onClick = { selectedFilter = ProjectFilter.ONGOING },
                    label = { Text("Ongoing") }
                )
                FilterChip(
                    selected = selectedFilter == ProjectFilter.COMPLETED,
                    onClick = { selectedFilter = ProjectFilter.COMPLETED },
                    label = { Text("Completed") }
                )
                FilterChip(
                    selected = selectedFilter == ProjectFilter.AT_RISK,
                    onClick = { selectedFilter = ProjectFilter.AT_RISK },
                    label = { Text("At Risk") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Projects List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredProjects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = {
                            navController.navigate(Screen.ProjectDetails.route)
                        }
                    )
                }
            }
        }
    }

    // ✅ Add Project Dialog
    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newProject ->
                projects.add(0, newProject) // add at top
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (ProjectUi) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ongoing") }
    var progressText by remember { mutableStateOf("0") }

    val statusOptions = listOf("Ongoing", "Completed", "At Risk")
    var statusMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Budget (numbers only)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = statusMenuOpen,
                    onExpandedChange = { statusMenuOpen = !statusMenuOpen }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = statusMenuOpen,
                        onDismissRequest = { statusMenuOpen = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusMenuOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = progressText,
                    onValueChange = { progressText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Progress % (0 - 100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val budget = budgetText.toIntOrNull() ?: 0
                    val progressPercent = progressText.toIntOrNull()?.coerceIn(0, 100) ?: 0
                    val progress = progressPercent / 100f

                    if (name.isBlank()) return@TextButton

                    onSave(
                        ProjectUi(
                            name = name.trim(),
                            status = status,
                            budget = budget,
                            progress = progress,
                            lastUpdated = "Just now"
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun MiniKpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectUi,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(status = project.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Budget: $${project.budget}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(progress = project.progress)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Progress: ${(project.progress * 100).roundToInt()}%  •  Updated: ${project.lastUpdated}",
                style = MaterialTheme.typography.labelMedium
            )
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

// ✅ UI model (keep it here for now, later we move to models/)
data class ProjectUi(
    val name: String,
    val status: String,
    val budget: Int,
    val progress: Float,
    val lastUpdated: String
)

private enum class ProjectFilter { ALL, ONGOING, COMPLETED, AT_RISK }


@Preview
@Composable
fun ProjectsScreenPreview() {
    ProjectsScreen(navController = rememberNavController())
}