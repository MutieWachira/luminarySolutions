package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    navController: NavController,
    projectId: String,
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share or more */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Unknown error")
                }
            } else {
                uiState.project?.let { project ->
                    ProjectDetailsContent(
                        project = project,
                        volunteers = uiState.volunteers,
                        onExpensesClick = {
                            navController.navigate(com.example.luminarysolutions.ui.navigation.Screen.Expenses.createRoute(project.id))
                        },
                        onToggleTask = { taskId, isDone ->
                            viewModel.toggleTaskStatus(project.id, taskId, isDone)
                        },
                        onAssignLeader = { leaderId ->
                            viewModel.assignGroupLeader(project.id, leaderId)
                        },
                        onAddTask = { title, assignedTo ->
                            viewModel.addTask(project.id, title, assignedTo)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDetailsContent(
    project: Project,
    volunteers: List<Volunteer>,
    onExpensesClick: () -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    onAssignLeader: (String) -> Unit,
    onAddTask: (String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Finance", "Activity")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Column {
                project.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_report_image),
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                }
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProjectStatusBadge(project.status)
                        Text(
                            project.lastUpdated,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        project.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(project.location.ifBlank { "Not specified" }, style = MaterialTheme.typography.labelMedium)
                        }
                        val startDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(project.startDate)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(startDateStr, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Progress Card
        ModernDetailCard(
            title = "Execution Progress",
            icon = Icons.Default.Timeline
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(project.progress * 100).roundToInt()}% Complete",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "On Track",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF10B981)
                    )
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Tab Row
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> OverviewTab(project, volunteers)
            1 -> FinanceTab(project, onExpensesClick = onExpensesClick)
            2 -> ActivityTab(
                project = project,
                volunteers = volunteers,
                onToggleTask = onToggleTask,
                onAssignLeader = onAssignLeader,
                onAddTask = onAddTask
            )
        }
    }
}


@Composable
fun OverviewTab(project: Project, volunteers: List<Volunteer>) {
    val startDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(project.startDate)
    val groupLeader = volunteers.find { it.id == project.groupLeaderId }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoRow(Icons.Default.Info, "Description", project.description.ifBlank { "No description provided." })
        InfoRow(Icons.Default.LocationOn, "Location", project.location.ifBlank { "Not specified" })
        InfoRow(Icons.Default.CalendarToday, "Start Date", startDateStr)
        InfoRow(Icons.Default.Person, "Group Leader", groupLeader?.name ?: "No leader assigned")
        InfoRow(Icons.Default.Groups, "Project Volunteers", if (project.volunteers.isEmpty()) "No volunteers yet" else "${project.volunteers.size} volunteers signed up")
    }
}

@Composable
fun FinanceTab(project: Project, onExpensesClick: () -> Unit = {}) {
    val spentPercent = if (project.budget > 0) project.spent.toFloat() / project.budget.toFloat() else 0f
    val remainingAmount = project.budget - project.spent
    val remainingPercent = 1f - spentPercent

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        FinanceMetric("Total Budget", "KES ${project.budget}", Color(0xFF6366F1))
        
        // Expense Bar
        Card(
            onClick = onExpensesClick,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Spent", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("KES ${project.spent}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { spentPercent },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
                Text(
                    "${(spentPercent * 100).toInt()}% of budget utilized",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Balance Bar
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Remaining Balance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("KES $remainingAmount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { remainingPercent },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun ActivityTab(
    project: Project,
    volunteers: List<Volunteer>,
    onToggleTask: (String, Boolean) -> Unit,
    onAssignLeader: (String) -> Unit,
    onAddTask: (String, String) -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showLeaderSelection by remember { mutableStateOf(false) }
    val leader = volunteers.find { it.id == project.groupLeaderId }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Group Leader Section
        Text("Leadership", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth().clickable { showLeaderSelection = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(leader?.name ?: "No Leader Assigned", fontWeight = FontWeight.Bold)
                    Text(if (leader != null) "Group Leader" else "Tap to assign a leader", style = MaterialTheme.typography.labelMedium)
                }
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tasks Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Project Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Task")
            }
        }
        
        if (project.tasks.isEmpty()) {
            Text("No tasks assigned to this project.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        } else {
            project.tasks.forEach { task ->
                TaskItem(task = task, onToggle = { onToggleTask(task.id, it) })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Activity History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ActivityItem("Budget Approved", "Executive board finalized funding", "2d ago")
        ActivityItem("Site Survey", "Initial logistics completed", "1w ago")
    }

    if (showLeaderSelection) {
        AlertDialog(
            onDismissRequest = { showLeaderSelection = false },
            title = { Text("Assign Group Leader") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (volunteers.isEmpty()) {
                        Text("No volunteers available to assign.")
                    }
                    volunteers.forEach { volunteer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAssignLeader(volunteer.id)
                                    showLeaderSelection = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(selected = volunteer.id == project.groupLeaderId, onClick = null)
                            Text(volunteer.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLeaderSelection = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var selectedAssignee by remember { mutableStateOf(leader?.name ?: "Unassigned") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Project Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Assign To:", style = MaterialTheme.typography.labelLarge)
                    
                    // Simple selection for assignee
                    Column {
                        listOf(leader?.name ?: "Group Leader", "General Team").forEach { assignee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAssignee = assignee }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedAssignee == assignee, onClick = null)
                                Text(assignee, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAddTask(taskTitle, selectedAssignee)
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Create Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TaskItem(task: com.example.luminarysolutions.data.models.Task, onToggle: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    "Assigned to: ${task.assignedTo}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (task.isDone) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FinanceMetric(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Black, color = color, fontSize = 18.sp)
        }
    }
}

@Composable
fun ActivityItem(title: String, desc: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.width(2.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ModernDetailCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
