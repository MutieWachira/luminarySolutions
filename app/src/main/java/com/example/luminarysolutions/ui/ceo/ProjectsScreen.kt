package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProjectsScreen(
    navController: NavController,
    viewModel: ProjectsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProjectsScreenContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onFilterChange = { viewModel.onFilterChange(it) },
        onAddProject = { project, uri -> viewModel.addProject(project, uri) },
        onUpdateProject = { project, uri -> viewModel.updateProject(project, uri) },
        onDeleteProject = { viewModel.deleteProject(it) },
        onProjectClick = { projectId ->
            navController.navigate(Screen.ProjectDetails.createRoute(projectId))
        },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreenContent(
    uiState: ProjectsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (ProjectFilter) -> Unit,
    onAddProject: (Project, Uri?) -> Unit,
    onUpdateProject: (Project, Uri?) -> Unit,
    onDeleteProject: (String) -> Unit,
    onProjectClick: (String) -> Unit,
    onBackClick: () -> Unit,
    isSearchingInitial: Boolean = uiState.searchQuery.isNotEmpty()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var isSearching by remember { mutableStateOf(isSearchingInitial) }

    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${projectToDelete?.name}'? This action cannot be undone and will remove all associated tasks and data.") },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.id?.let { onDeleteProject(it) }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search by program name...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(end = 16.dp)
                        )
                    } else {
                        Text(
                            "Programs & Projects",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearching = !isSearching 
                        if (!isSearching) onSearchQueryChange("")
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Launch Program", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProjectOverviewStats(
                    projects = uiState.projects,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                ProjectFilterRow(
                    selectedFilter = uiState.filter,
                    onFilterSelected = onFilterChange
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (uiState.projects.isEmpty()) {
                item {
                    EmptyProjectsState(
                        isSearching = uiState.searchQuery.isNotEmpty(),
                        onAddClick = { showAddDialog = true }
                    )
                }
            } else {
                items(uiState.projects, key = { it.id }) { project ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    projectToDelete = project
                                }
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    projectToEdit = project
                                }
                                else -> {}
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF43F5E)
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF6366F1)
                                    else -> Color.Transparent
                                },
                                label = "swipeColor"
                            )
                            
                            val alignment = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                else -> Alignment.Center
                            }
                            
                            val icon = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                else -> Icons.Default.Delete
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(color)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        content = {
                            DetailedProjectCard(
                                project = project,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable { onProjectClick(project.id) }
                            )
                        }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddEditProjectDialog(
            isSaving = uiState.isSaving,
            allTeamMembers = uiState.teamMembers,
            onDismiss = { showAddDialog = false },
            onSave = { project, uri ->
                onAddProject(project, uri)
            }
        )
    }

    if (projectToEdit != null) {
        AddEditProjectDialog(
            project = projectToEdit,
            isSaving = uiState.isSaving,
            allTeamMembers = uiState.teamMembers,
            onDismiss = { projectToEdit = null },
            onSave = { project, uri ->
                onUpdateProject(project, uri)
            }
        )
    }

    LaunchedEffect(uiState.isSaving) {
        if (!uiState.isSaving) {
            showAddDialog = false
            projectToEdit = null
        }
    }
}

@Composable
fun ProjectOverviewStats(projects: List<Project>, modifier: Modifier = Modifier) {
    val totalProjects = projects.size
    val completedProjects = projects.count { it.status == "Completed" }
    val ongoingProjects = projects.count { it.status == "Ongoing" }
    val atRiskProjects = projects.count { it.status == "At Risk" }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectStatusMiniCard(
                label = "Total Programs",
                value = totalProjects.toString(),
                icon = Icons.Default.Layers,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            ProjectStatusMiniCard(
                label = "Completed",
                value = completedProjects.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectStatusMiniCard(
                label = "Ongoing",
                value = ongoingProjects.toString(),
                icon = Icons.Default.PlayArrow,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            ProjectStatusMiniCard(
                label = "At Risk",
                value = atRiskProjects.toString(),
                icon = Icons.Default.Warning,
                color = Color(0xFFF43F5E),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProjectStatusMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProjectFilterRow(
    selectedFilter: ProjectFilter,
    onFilterSelected: (ProjectFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        items(ProjectFilter.entries.toTypedArray()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = null
            )
        }
    }
}

@Composable
fun DetailedProjectCard(
    project: Project,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Image
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(88.dp)
                ) {
                    if (!project.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = project.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Business,
                                null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Title and Info
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            project.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        ProjectStatusBadge(project.status)
                    }
                    
                    Text(
                        project.category.ifBlank { "General Impact" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(project.location.ifBlank { "Global" }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        
                        // Volunteer Count Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.VolunteerActivism, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(10.dp))
                                Text(
                                    "${project.volunteers.size} Volunteers", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Description
            if (project.description.isNotBlank()) {
                Text(
                    project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "IMPACT DELIVERY", 
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), 
                        fontWeight = FontWeight.Black, 
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                    Text(
                        "${(project.progress * 100).toInt()}%", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black,
                        color = getProjectStatusColor(project.status)
                    )
                }
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = getProjectStatusColor(project.status),
                    trackColor = Color(0xFFF1F5F9)
                )
            }

            // Metrics Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricItem(Icons.Default.AccountBalanceWallet, "KES ${formatBudget(project.budget)}")
                    MetricItem(Icons.Default.Badge, "${project.teamMemberIds.size} Luminary Staff")
                }
                
                Text(
                    "UPDATED ${project.lastUpdated.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun MetricItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

fun formatBudget(amount: Int): String {
    return when {
        amount >= 1_000_000 -> "${String.format("%.1f", amount / 1_000_000f)}M"
        amount >= 1_000 -> "${String.format("%.0f", amount / 1_000f)}K"
        else -> amount.toString()
    }
}

@Composable
fun ProjectStatusBadge(status: String) {
    val color = getProjectStatusColor(status)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.sp
        )
    }
}

fun getProjectStatusColor(status: String): Color = when (status) {
    "Completed" -> Color(0xFF10B981)
    "At Risk" -> Color(0xFFF43F5E)
    "Ongoing" -> Color(0xFF3B82F6)
    else -> Color(0xFF6B7280)
}

@Composable
fun EmptyProjectsState(isSearching: Boolean, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isSearching) "No matching programs" else "No programs available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        if (!isSearching) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Your First Program")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProjectDialog(
    project: Project? = null,
    isSaving: Boolean = false,
    allTeamMembers: List<com.example.luminarysolutions.data.models.User> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Project, Uri?) -> Unit
) {
    val isEdit = project != null
    
    // Form States
    var name by remember { mutableStateOf(project?.name ?: "") }
    var category by remember { mutableStateOf(project?.category ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var location by remember { mutableStateOf(project?.location ?: "") }
    var budget by remember { mutableStateOf(project?.budget?.toString() ?: "") }
    var spent by remember { mutableStateOf(project?.spent?.toString() ?: "") }
    var progress by remember { mutableFloatStateOf(project?.progress ?: 0f) }
    var status by remember { mutableStateOf(project?.status ?: "Ongoing") }
    var imageUrl by remember { mutableStateOf(project?.imageUrl ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var startDate by remember { mutableLongStateOf(project?.startDate ?: System.currentTimeMillis()) }
    
    var client by remember { mutableStateOf(project?.client ?: "") }
    var groupLeaderId by remember { mutableStateOf(project?.groupLeaderId ?: "") }
    var selectedTeamMemberIds by remember { mutableStateOf(project?.teamMemberIds?.toSet() ?: emptySet()) }
    
    // UI States
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    var statusExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var teamExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = listOf("Education", "Health", "Environment", "Water", "Community", "Infrastructure", "General")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = if (isSaving) ({}) else onDismiss,
        confirmButton = {
            Button(
                onClick = { 
                    val newProject = Project(
                        id = project?.id ?: "",
                        name = name,
                        status = status,
                        budget = budget.toIntOrNull() ?: 0,
                        spent = spent.toIntOrNull() ?: 0,
                        progress = progress,
                        lastUpdated = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date()),
                        imageUrl = if (selectedImageUri != null) null else imageUrl,
                        description = description,
                        location = location,
                        startDate = datePickerState.selectedDateMillis ?: startDate,
                        client = client,
                        groupLeaderId = groupLeaderId,
                        category = category,
                        tasks = project?.tasks ?: emptyList(),
                        volunteers = project?.volunteers ?: emptyList(),
                        groupLeaderIds = project?.groupLeaderIds ?: emptyList(),
                        teamMemberIds = selectedTeamMemberIds.toList(),
                        clients = project?.clients ?: emptyList()
                    )
                    onSave(newProject, selectedImageUri)
                },
                enabled = name.isNotBlank() && category.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(16.dp)
            ) { 
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Synchronizing...")
                } else {
                    Text(if (isEdit) "Update Program" else "Deploy Program") 
                }
            }
        },
        dismissButton = { 
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) { Text("Discard") } 
        },
        title = { 
            Column {
                Text(
                    if (isEdit) "Configure Program" else "Launch New Program", 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Set up the vision and team for this initiative.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section: Visual Identity
                Text("VISUAL IDENTITY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Surface(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { launcher.launch("image/*") },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop)
                        } else if (imageUrl.isNotBlank()) {
                            AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Add Cover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section: Core Details
                Text("CORE INFORMATION", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Program Title") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("e.g. Sustainable Solar Grids") }
                )
                
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Impact Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { categoryExpanded = true }) { Icon(Icons.Default.Category, null) } },
                        shape = RoundedCornerShape(16.dp),
                        readOnly = true
                    )
                    DropdownMenu(
                        expanded = categoryExpanded, 
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, fontWeight = FontWeight.Medium) }, 
                                onClick = { category = c; categoryExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Mission Description") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    minLines = 3,
                    maxLines = 6
                )

                // Section: Luminary Team & Talent
                Text("LUMINARY TEAM & TALENT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (selectedTeamMemberIds.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedTeamMemberIds.forEach { id ->
                                val member = allTeamMembers.find { it.id == id }
                                member?.let {
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedTeamMemberIds = selectedTeamMemberIds - id },
                                        label = { Text(it.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                        avatar = {
                                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(it.name.take(1).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                                        shape = CircleShape,
                                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                teamExpanded = true
                            },
                            label = { Text("Assign Staff Members") },
                            placeholder = { Text("Search by name or role...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.GroupAdd, null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                                }
                            }
                        )
                        
                        val filteredTeam = allTeamMembers.filter { 
                            it.role == UserRole.TEAM &&
                            (it.name.contains(searchQuery, ignoreCase = true) || 
                            it.role.name.contains(searchQuery, ignoreCase = true))
                        }

                        DropdownMenu(
                            expanded = teamExpanded && (searchQuery.isNotEmpty() || filteredTeam.isNotEmpty()),
                            onDismissRequest = { teamExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 300.dp),
                            properties = PopupProperties(focusable = false)
                        ) {
                            if (filteredTeam.isEmpty()) {
                                DropdownMenuItem(text = { Text("No results found") }, onClick = { teamExpanded = false })
                            } else {
                                filteredTeam.forEach { member ->
                                    val isSelected = selectedTeamMemberIds.contains(member.id)
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = isSelected, onCheckedChange = null)
                                                Spacer(Modifier.width(12.dp))
                                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                                    }
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text(member.name, fontWeight = FontWeight.Bold)
                                                    Text(member.role.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedTeamMemberIds = if (isSelected) {
                                                selectedTeamMemberIds - member.id
                                            } else {
                                                selectedTeamMemberIds + member.id
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Logistics & Finance
                Text("LOGISTICS & FINANCE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = budget, 
                        onValueChange = { budget = it }, 
                        label = { Text("Budget (KES)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(16.dp),
                        prefix = { Text("KES ", fontWeight = FontWeight.Bold, color = Color.Gray) }
                    )
                    OutlinedTextField(
                        value = spent, 
                        onValueChange = { spent = it }, 
                        label = { Text("Spent (KES)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(16.dp),
                        prefix = { Text("KES ", fontWeight = FontWeight.Bold, color = Color.Gray) }
                    )
                }

                OutlinedTextField(
                    value = location, 
                    onValueChange = { location = it }, 
                    label = { Text("Operational Location") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    leadingIcon = { Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary) }
                )

                OutlinedTextField(
                    value = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(datePickerState.selectedDateMillis ?: startDate)),
                    onValueChange = {},
                    label = { Text("Kick-off Date") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Section: Status & Progress
                Text("STATUS & PROGRESS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Box {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Deployment Status") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { statusExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        shape = RoundedCornerShape(16.dp)
                    )
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        listOf("Ongoing", "Completed", "At Risk", "Planned").forEach { s ->
                            DropdownMenuItem(text = { Text(s, fontWeight = FontWeight.Medium) }, onClick = { status = s; statusExpanded = false })
                        }
                    }
                }

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Impact Progress", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = progress, 
                        onValueChange = { progress = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }

                OutlinedTextField(
                    value = client, 
                    onValueChange = { client = it }, 
                    label = { Text("Anchor Partner / Sponsor") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Handshake, null) }
                )
            }
        },
        shape = RoundedCornerShape(32.dp)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { Button(onClick = { showDatePicker = false }, shape = RoundedCornerShape(12.dp)) { Text("Set Date") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreview() {
    ProjectsScreenContent(
        uiState = ProjectsUiState(
            projects = listOf(
                Project(
                    id = "1",
                    name = "Rural Electrification",
                    status = "Ongoing",
                    budget = 5000000,
                    spent = 1200000,
                    progress = 0.35f,
                    lastUpdated = "2h ago",
                    category = "Infrastructure",
                    location = "Turkana, Kenya"
                )
            ),
            isLoading = false
        ),
        onSearchQueryChange = {},
        onFilterChange = {},
        onAddProject = { _, _ -> },
        onUpdateProject = { _, _ -> },
        onDeleteProject = {},
        onProjectClick = {},
        onBackClick = {}
    )
}
