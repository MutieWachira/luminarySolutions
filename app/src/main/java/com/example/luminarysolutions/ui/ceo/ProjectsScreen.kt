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
import com.example.luminarysolutions.ui.ceo.data.ProjectsStore
import com.example.luminarysolutions.ui.ceo.models.ProjectUi
import com.example.luminarysolutions.ui.navigation.Screen
import java.util.UUID
import kotlin.math.roundToInt

private enum class ProjectFilter { ALL, ONGOING, COMPLETED, AT_RISK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(navController: NavController) {

    // ✅ IMPORTANT: use the SAME source as ProjectDetailsScreen
    val projects = ProjectsStore.projects

    var selectedFilter by remember { mutableStateOf(ProjectFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    // ✅ Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredProjects = remember(selectedFilter, projects, searchQuery) {
        projects
            .filter { project ->
                when (selectedFilter) {
                    ProjectFilter.ALL -> true
                    ProjectFilter.ONGOING -> computeStatus(project) == "Ongoing"
                    ProjectFilter.COMPLETED -> computeStatus(project) == "Completed"
                    ProjectFilter.AT_RISK -> computeStatus(project) == "At Risk"
                }
            }
            .filter { project ->
                project.name.contains(searchQuery, ignoreCase = true)
            }
    }

    val total = projects.size
    val ongoing = projects.count { computeStatus(it) == "Ongoing" }
    val completed = projects.count { computeStatus(it) == "Completed" }
    val atRisk = projects.count { computeStatus(it) == "At Risk" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search projects...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Projects & Operations")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSearching) searchQuery = ""
                            isSearching = !isSearching
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniKpiCard("Total", total.toString(), Modifier.weight(1f))
                MiniKpiCard("Ongoing", ongoing.toString(), Modifier.weight(1f))
                MiniKpiCard("Complete", completed.toString(), Modifier.weight(1f))
                MiniKpiCard("At Risk", atRisk.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredProjects) { project ->
                    ProjectCard(
                        project = project,
                        status = computeStatus(project),
                        onClick = {
                            // ✅ FIX: navigate WITH the actual projectId
                            navController.navigate(Screen.ProjectDetails.createRoute(project.id))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newProject ->
                // ✅ FIX: add into store so details can find it
                ProjectsStore.addProject(newProject)
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
    var progressText by remember { mutableStateOf("0") }

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
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            status = "Ongoing",
                            budget = budget,
                            spent = 0,
                            progress = progress,
                            lastUpdated = "Today"
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MiniKpiCard(title: String, value: String, modifier: Modifier = Modifier) {
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
    status: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusChip(status = status)
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
    AssistChip(onClick = { }, label = { Text(status) })
}

private fun computeStatus(p: ProjectUi): String {
    val progressPercent = (p.progress * 100).toInt()
    if (progressPercent >= 100) return "Completed"

    val isNew = p.lastUpdated.equals("Today", true) || p.lastUpdated.contains("Just now", true)
    if (isNew) return "Ongoing"

    val spentPercent =
        if (p.budget == 0) 0f else (p.spent.toFloat() / p.budget.toFloat()).coerceIn(0f, 1f)

    val budgetBurnTooHigh = spentPercent > (p.progress + 0.25f)
    val behindSchedule = p.progress < 0.5f
    val staleUpdate = p.lastUpdated.contains("week", ignoreCase = true)

    return if (budgetBurnTooHigh || behindSchedule || staleUpdate) "At Risk" else "Ongoing"
}

@Preview
@Composable
fun ProjectsScreenPreview() {
    ProjectsScreen(navController = rememberNavController())
}
