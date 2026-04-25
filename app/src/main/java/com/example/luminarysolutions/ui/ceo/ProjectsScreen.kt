package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.ui.navigation.Screen
import java.text.SimpleDateFormat
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
        onAddProject = { name, budget, description, location, startDate, imageUrl, imageUri -> 
            viewModel.addProject(name, budget, description, location, startDate, imageUrl, imageUri)
        },
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
    onAddProject: (String, Int, String, String, Long, String?, Uri?) -> Unit,
    onProjectClick: (String) -> Unit,
    onBackClick: () -> Unit,
    isSearchingInitial: Boolean = uiState.searchQuery.isNotEmpty()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(isSearchingInitial) }

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
                                    "Search projects...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(end = 8.dp)
                        )
                    } else {
                        Text(
                            "Projects",
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Project", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProjectSummaryHeader(
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
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
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
                    ModernProjectListItem(
                        project = project,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { onProjectClick(project.id) }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, budget, description, location, startDate, imageUrl, imageUri ->
                onAddProject(name, budget, description, location, startDate, imageUrl, imageUri)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectSummaryHeader(projects: List<Project>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val totalBudget = projects.sumOf { it.budget.toLong() }
        val avgProgress = if (projects.isEmpty()) 0f else projects.map { it.progress }.average().toFloat()

        KpiCard(
            label = "Projects",
            value = projects.size.toString(),
            icon = Icons.Default.Layers,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Budget",
            value = "KES ${(totalBudget / 1_000_000)}M",
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Progress",
            value = "${(avgProgress * 100).toInt()}%",
            icon = Icons.Default.AutoGraph,
            color = Color(0xFF6366F1),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun KpiCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
fun ModernProjectListItem(project: Project, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            project.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    //find out how to fit the image well.
                    contentScale = ContentScale.FillBounds,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            project.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Budget: KES ${project.budget}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    ProjectStatusBadge(project.status)
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { project.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = getProjectStatusColor(project.status),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${(project.progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text(project.lastUpdated, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
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
            fontWeight = FontWeight.ExtraBold
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
            text = if (isSearching) "No matching projects" else "No projects available",
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
                Text("Add Your First Project")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String, Long, String?, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { 
                    onSave(
                        name, 
                        budget.toIntOrNull() ?: 0, 
                        description,
                        location,
                        datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                        imageUrl.ifBlank { null }, 
                        selectedImageUri
                    ) 
                },
                enabled = name.isNotBlank() && budget.isNotBlank() && description.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Create Project") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Discard") } },
        title = { Text("Initiate New Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Project Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Budget (KES)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = if (datePickerState.selectedDateMillis != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(datePickerState.selectedDateMillis)
                    } else "Select Start Date",
                    onValueChange = {},
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                HorizontalDivider()

                // Image Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Project Image", style = MaterialTheme.typography.labelLarge)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { launcher.launch("image/*") },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (selectedImageUri != null) "Gallery Image Selected" else "Select from Gallery",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedImageUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { launcher.launch("image/*") }
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (selectedImageUri != null) "This image will be uploaded" else "Or enter a URL manually below",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = if (selectedImageUri != null) "Using gallery image..." else imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("https://example.com/image.jpg") },
                        enabled = selectedImageUri == null,
                        readOnly = selectedImageUri != null,
                        trailingIcon = {
                            if (selectedImageUri != null) {
                                IconButton(onClick = { selectedImageUri = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear gallery image")
                                }
                            }
                        }
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenSearchingPreview() {
    ProjectsScreenContent(
        uiState = ProjectsUiState(
            projects = listOf(
                Project(id = "1", name = "Project Alpha", status = "Ongoing", budget = 1000000, spent = 200000, progress = 0.5f, lastUpdated = "Today", imageUrl = "https://images.unsplash.com/photo-1541888946425-d81bb19480c5?q=80&w=2070&auto=format&fit=crop")
            ),
            isLoading = false,
            searchQuery = "Alpha"
        ),
        onSearchQueryChange = {},
        onFilterChange = {},
        onAddProject = { _, _, _, _, _, _, _ -> },
        onProjectClick = {},
        onBackClick = {}
    )
}
