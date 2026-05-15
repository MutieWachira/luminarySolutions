package com.example.luminarysolutions.ui.ceo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.ui.ceo.FinancialSummaryCard
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaryDetailsScreen(
    navController: NavController,
    dashboardViewModel: CEODashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val selectedYear by dashboardViewModel.selectedYear.collectAsState()

    LuminaryDetailsContent(
        uiState = uiState,
        selectedYear = selectedYear,
        onYearSelected = { dashboardViewModel.updateSelectedYear(it) },
        onAddProject = { dashboardViewModel.addLuminaryProject(it) { } },
        onDeleteProject = { dashboardViewModel.deleteLuminaryProject(it) { } },
        onUpdateProject = { dashboardViewModel.updateLuminaryProject(it) { } },
        onBackClick = { navController.popBackStack() },
        onProjectClick = { projectId ->
            navController.navigate(com.example.luminarysolutions.ui.navigation.Screen.ProjectDetails.createRoute(projectId))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaryDetailsContent(
    uiState: CEODashboardUiState,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onAddProject: (Project) -> Unit,
    onDeleteProject: (String) -> Unit,
    onUpdateProject: (Project) -> Unit,
    onBackClick: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    // Local state for tab navigation within the details screen
    var selectedTabIndex by remember { mutableIntStateOf(2) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }

    if (showAddProjectDialog) {
        AddProjectDialog(
            teamMembers = uiState.teamMembers,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { project ->
                onAddProject(project)
                showAddProjectDialog = false
            }
        )
    }

    if (projectToEdit != null) {
        EditProjectDialog(
            project = projectToEdit!!,
            teamMembers = uiState.teamMembers,
            onDismiss = { projectToEdit = null },
            onConfirm = { updatedProject ->
                onUpdateProject(updatedProject)
                projectToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Luminary Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Business Overview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More actions */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section (Dark Card) - Always visible
            LuminaryHeaderCard()

            // Tabs Row - Controlling which content to display
            LuminaryTabsRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            // Dynamic Content based on selected tab
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> OverviewTabContent(uiState, selectedYear, onYearSelected)
                    1 -> FinancialsTabContent()
                    2 -> ProjectsTabContent(
                        uiState = uiState,
                        onAddProjectClick = { showAddProjectDialog = true },
                        onDeleteProject = onDeleteProject,
                        onEditProject = { projectToEdit = it },
                        onProjectClick = onProjectClick
                    )
                    3 -> PerformanceTabContent()
                    4 -> DocumentsTabContent()
                    5 -> TeamTabContent()
                    else -> {
                        // Placeholder for other tabs
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Content for ${listOf("Overview", "Financials", "Projects", "Performance", "Documents", "Team")[selectedTabIndex]} coming soon", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Content for the Overview Tab
 */
@Composable
fun OverviewTabContent(
    uiState: CEODashboardUiState,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            "Key Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )

        // Key Metrics Grid - Real-time data from ViewModel
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryCard(
                    label = "Revenue (YTD)",
                    value = formatAmount(uiState.lumStats.totalRevenue),
                    trend = "+10.3%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryCard(
                    label = "Profit (YTD)",
                    value = formatAmount(uiState.lumStats.totalProfit),
                    trend = "+18.7%",
                    icon = Icons.Default.Payments,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryCard(
                    label = "Total Projects",
                    value = uiState.lumStats.totalProjects.toString(),
                    trend = "+12%",
                    icon = Icons.Default.BusinessCenter,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryCard(
                    label = "Active Clients",
                    value = uiState.lumStats.totalActiveClient.toString(),
                    trend = "+5.2%",
                    icon = Icons.Default.Person,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Financial Overview - Real-time breakdown
        FinancialOverviewSection(uiState.lumStats, selectedYear, onYearSelected)

        // Business Portfolio
        BusinessPortfolioSection()

        // Recent Projects & Documents
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecentProjectsSection(
                modifier = Modifier.weight(1f),
                projects = uiState.initiatives.take(3)
            )
            RecentDocumentsSection(
                modifier = Modifier.weight(1f),
                documents = uiState.documents
            )
        }

        // Insights
        InsightsCard()
    }
}

/**
 * Content for the Financials Tab - Based on the provided design image
 */
@Composable
fun FinancialsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Date Selector Row
        Surface(
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text("Jan 1 — May 20, 2023", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
            }
        }

        Text("Financial Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)

        // Summary Cards - 2x2 Grid for better readability on mobile
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryCard(
                    label = "Total Revenue (YTD)",
                    value = "$1.25M",
                    trend = "+10.3%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryCard(
                    label = "Total Expenses (YTD)",
                    value = "$645K",
                    trend = "-4.5%",
                    icon = Icons.Default.Payments,
                    color = Color(0xFFF43F5E), // Red for expenses
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryCard(
                    label = "Net Profit (YTD)",
                    value = "$605K",
                    trend = "+18.7%",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryCard(
                    label = "Profit Margin (YTD)",
                    value = "48.4%",
                    trend = "+5.2%",
                    icon = Icons.Default.PieChart,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Revenue vs Expenses Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Revenue vs Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("View full report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            FinancialOverviewChart()
        }

        // Expense Breakdown
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expense Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("View details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            ExpenseBreakdownSection()
        }

        // Cash Flow & Budget
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CashFlowSection(Modifier.weight(1f))
            BudgetUtilizationSection(Modifier.weight(1f))
        }

        // Recent Transactions
        RecentTransactionsSection()
        
        // Insights
        InsightsCard()
    }
}

/**
 * Content for the Projects Tab
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsTabContent(
    uiState: CEODashboardUiState,
    onAddProjectClick: () -> Unit,
    onDeleteProject: (String) -> Unit,
    onEditProject: (Project) -> Unit,
    onProjectClick: (String) -> Unit
) {
    val projects = uiState.luminaryProjects
    val totalProjects = projects.size
    val completedProjects = projects.count { it.status == "Completed" }
    val inProgressProjects = projects.count { it.status == "In Progress" }
    val onHoldProjects = projects.count { it.status == "On Hold" }

    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete '${projectToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.id?.let { onDeleteProject(it) }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Project Overview Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Projects Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            
            Button(
                onClick = onAddProjectClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Project", fontSize = 12.sp)
            }
        }

        // Project Overview Stats in a 2x2 Grid
       Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProjectStatusMiniCard(
                    label = "Total Projects",
                    value = totalProjects.toString(),
                    trend = "+${(totalProjects * 0.1).toInt()}%",
                    icon = Icons.Default.Inventory2,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f) // Makes it take half width
                )
                ProjectStatusMiniCard(
                    label = "Completed",
                    value = completedProjects.toString(),
                    trend = "+2",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProjectStatusMiniCard(
                    label = "In Progress",
                    value = inProgressProjects.toString(),
                    trend = "+3",
                    icon = Icons.Default.Pending,
                    color = Color(0xFF0EA5E9),
                    modifier = Modifier.weight(1f)
                )
                ProjectStatusMiniCard(
                    label = "On Hold",
                    value = onHoldProjects.toString(),
                    trend = "-1",
                    icon = Icons.Default.PauseCircle,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }


        // Search and Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search projects...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8F9FA), unfocusedBorderColor = Color.Transparent)
            )
            
            FilterDropdown("All Status")
            FilterDropdown("Sort: Newest")
            
            IconButton(onClick = {}) {
                Icon(Icons.Default.FilterList, null)
            }
        }

        // Project List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No projects found", color = Color.Gray)
                }
            } else {
                projects.forEach { project ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.EndToStart -> projectToDelete = project
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onEditProject(project)
                                }
                                else -> {}
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFF43F5E) // Red for delete
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF6366F1) // Indigo for edit
                                else -> Color.Transparent
                            }
                            
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
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
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
                                name = project.name,
                                status = project.status,
                                imageUrl = project.imageUrl,
                                client = "${project.client} • ${project.category}",
                                description = project.description,
                                dates = "Started: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(project.startDate))}",
                                teamSize = project.volunteers.size,
                                progress = project.progress,
                                budget = "$${String.format("%,d", project.budget)}",
                                spent = "$${String.format("%,d", project.spent)}",
                                indicator = if (project.spent > project.budget) "Over Budget" else "On Track",
                                indicatorColor = if (project.spent > project.budget) Color.Red else Color(0xFF10B981),
                                modifier = Modifier.clickable { onProjectClick(project.id) }
                            )
                        }
                    )
                }
            }
        }
        
        // Pagination
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Showing 1-${projects.size} of $totalProjects projects", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaginationButton("<", false)
                PaginationButton("1", true)
                PaginationButton("2", false)
                PaginationButton("3", false)
                PaginationButton("...", false)
                PaginationButton("5", false)
                PaginationButton(">", false)
            }
        }
    }
}

@Composable
fun ProjectStatusMiniCard(
    label: String,
    value: String,
    trend: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = if (trend.startsWith("+")) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null,
                    tint = if (trend.startsWith("+") && color != Color(0xFFF43F5E)) Color(0xFF10B981) else Color(0xFFF43F5E),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (trend.startsWith("+") && color != Color(0xFFF43F5E)) Color(0xFF10B981) else Color(0xFFF43F5E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text("vs last qtr", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun FilterDropdown(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}

@Composable
fun DetailedProjectCard(
    name: String,
    status: String,
    imageUrl: String?,
    client: String,
    description: String,
    dates: String,
    teamSize: Int,
    progress: Float,
    budget: String,
    spent: String,
    indicator: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section: Image + Title/Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Project Image - Compact for mobile
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Project Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Business,
                                null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Title, Status, and Client
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when(status) {
                                "In Progress" -> Color(0xFF0EA5E9).copy(alpha = 0.1f)
                                "Completed" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when(status) {
                                    "In Progress" -> Color(0xFF0EA5E9)
                                    "Completed" -> Color(0xFF10B981)
                                    else -> Color.Gray
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(client, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                }
            }

            // Description Section
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // Info and Metrics
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info Row (Dates & Team)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(dates, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Group, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text("Team: $teamSize", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Metrics Row (Progress + Financials + Indicator)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = when {
                                progress >= 1f -> Color(0xFF10B981)
                                progress >= 0.7f -> Color(0xFF0EA5E9)
                                else -> Color(0xFF6366F1)
                            },
                            trackColor = Color(0xFFF1F5F9)
                        )
                    }

                    // Financials and Indicator
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Budget", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                                Text(budget, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Spent", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                                Text(spent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(indicatorColor))
                            Text(indicator, style = MaterialTheme.typography.labelSmall, color = indicatorColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationButton(text: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun LuminaryHeaderCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF111418)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Luminary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Business",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    "A consulting and investment firm driving sustainable growth through innovation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 3
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderBadge(Icons.Default.CalendarToday, "Est. 2026")
                    HeaderBadge(Icons.Default.LocationOn, "Nairobi")
                    HeaderBadge(Icons.Default.Circle, "Active", Color(0xFF10B981))
                }


            }

            // Gauge
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 0.87f },
                    modifier = Modifier.size(70.dp),
                    color = Color(0xFF10B981),
                    strokeWidth = 8.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "8.7",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "Excellent",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderBadge(icon: ImageVector, text: String, color: Color = Color.Gray) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(10.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 6.sp)
    }
}

@Composable
fun LuminaryTabsRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Overview", "Financials", "Projects", "Performance", "Documents", "Team")
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 20.dp,
        divider = {},
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(selectedTabIndex),
                color = MaterialTheme.colorScheme.primary,
                height = 3.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (index == selectedTabIndex) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == selectedTabIndex) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            )
        }
    }
}

@Composable
fun FinancialOverviewChart() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Monthly Comparison", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("Revenue vs Expenses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("Rev", Color(0xFF6366F1))
                    Spacer(Modifier.width(12.dp))
                    LegendItem("Exp", Color(0xFFE2E8F0))
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Mock Bar Chart implementation with labels
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                val revData = listOf(0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 0.9f)
                val expData = listOf(0.3f, 0.4f, 0.35f, 0.5f, 0.45f, 0.55f)
                
                months.forEachIndexed { i, month ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(10.dp).fillMaxHeight(revData[i]).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color(0xFF6366F1)))
                            Box(modifier = Modifier.width(10.dp).fillMaxHeight(expData[i]).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color(0xFFE2E8F0)))
                        }
                        Text(month, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(
    label: String,
    value: String,
    trend: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = if (trend.startsWith("+")) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null,
                    tint = if (trend.startsWith("+") && color != Color(0xFFF43F5E) || trend.startsWith("-") && color == Color(0xFFF43F5E)) Color(0xFF10B981) else Color(0xFFF43F5E),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (trend.startsWith("+") && color != Color(0xFFF43F5E) || trend.startsWith("-") && color == Color(0xFFF43F5E)) Color(0xFF10B981) else Color(0xFFF43F5E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text("vs last qtr", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ExpenseBreakdownSection() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE2E8F0),
                    strokeWidth = 14.dp
                )
                CircularProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6366F1),
                    strokeWidth = 14.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$645K", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BreakdownItem("Operations", "$290K", "45%", Color(0xFF6366F1))
                BreakdownItem("Salaries", "$161K", "25%", Color(0xFF10B981))
                BreakdownItem("Marketing", "$96K", "15%", Color(0xFFF59E0B))
                BreakdownItem("Tech", "$64K", "10%", Color(0xFFF43F5E))
                BreakdownItem("Other", "$32K", "5%", Color.Gray)
            }
        }
    }
}

@Composable
fun BreakdownItem(label: String, value: String, percentage: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, fontSize = 8.sp)
        }
        Text("$value ($percentage)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@Composable
fun CashFlowSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cash Flow (YTD)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CashFlowItem("Cash Inflow", "$1.25M", Color(0xFF10B981))
            CashFlowItem("Cash Outflow", "$645K", Color(0xFFF43F5E))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            CashFlowItem("Net Cash Flow", "$605K", Color(0xFF6366F1), isTotal = true)
        }
    }
}

@Composable
fun CashFlowItem(label: String, value: String, color: Color, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = if (isTotal) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelSmall, color = if (isTotal) Color.Black else Color.Gray)
        Text(value, style = if (isTotal) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun BudgetUtilizationSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Budget Utilization", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("View details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE2E8F0),
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = { 0.66f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6366F1),
                    strokeWidth = 8.dp
                )
                Text("66%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BudgetStat("Total Budget", "$1.5M")
                BudgetStat("Utilized", "$990K")
                BudgetStat("Remaining", "$510K")
            }
        }
    }
}

@Composable
fun BudgetStat(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Gray))
        Text("$label: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@Composable
fun RecentTransactionsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Transactions", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TransactionItem("Payment received from ABC Consulting", "Consulting Service", "May 15, 2023", "+$12,000", Color(0xFF10B981))
            TransactionItem("Payment to Tech Solution Ltd", "IT Services", "May 12, 2023", "-$2,500", Color(0xFFF43F5E))
            TransactionItem("Team Salaries - May 2023", "Payroll", "May 10, 2023", "-$75,400", Color(0xFFF43F5E))
        }
        
        Text("See all transactions ->", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 8.sp)
    }
}

@Composable
fun TransactionItem(title: String, category: String, date: String, amount: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (amount.startsWith("+")) Icons.Default.AddCircle else Icons.Default.RemoveCircle, null, tint = color, modifier = Modifier.size(14.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp)
            Text("$category • $date", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
        }
        Text(amount, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color, fontSize = 9.sp)
    }
}

@Composable
fun FinancialOverviewSection(
    stats: com.example.luminarysolutions.data.firebase.lumOverviewDashboardStats,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val profitMargin = if (stats.totalRevenue > 0) {
        (stats.totalProfit.toFloat() / stats.totalRevenue.toFloat() * 100)
    } else 0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Financial Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            
            // Year Selector for selecting previous years
            YearSelector(
                selectedYear = selectedYear,
                onYearSelected = onYearSelected
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8F9FA),
            modifier = Modifier.fillMaxWidth().height(250.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    LegendItem("Revenue", Color(0xFF6366F1))
                    Spacer(Modifier.width(16.dp))
                    LegendItem("Expenses", Color(0xFFE2E8F0))
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Real-time Visual Representation using data from database
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (stats.monthlyStats.isEmpty()) {
                        // Debug view: Show if data is actually arriving
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No data found for $selectedYear\nCheck DB: luminary/financials/years/$selectedYear/months",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        // Industry Best Practice: Use 'key' for efficient list rendering in Compose
                        val maxValRaw = stats.monthlyStats.maxOfOrNull { maxOf(it.revenue, it.expenses) } ?: 0
                        val maxVal = if (maxValRaw > 0) maxValRaw.toFloat() * 1.1f else 1f
                        
                        stats.monthlyStats.forEach { monthStat ->
                            key(monthStat.month) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Revenue Bar with proportional height
                                        val revHeight = (monthStat.revenue / maxVal).coerceIn(0f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .fillMaxHeight(revHeight.coerceAtLeast(if (monthStat.revenue > 0) 0.05f else 0f))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Color(0xFF6366F1))
                                        )
                                        // Expenses Bar
                                        val expHeight = (monthStat.expenses / maxVal).coerceIn(0f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .fillMaxHeight(expHeight.coerceAtLeast(if (monthStat.expenses > 0) 0.05f else 0f))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Color(0xFFE2E8F0))
                                        )
                                    }
                                    // Month Label (e.g., "jan" -> "Jan")
                                    Text(
                                        text = monthStat.month.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FinancialStat("Revenue", formatAmount(stats.totalRevenue))
            FinancialStat("Expenses", formatAmount(stats.totalExpenses))
            FinancialStat("Profit", formatAmount(stats.totalProfit))
            FinancialStat("Profit Margin", "${String.format("%.1f", profitMargin)}%")
        }
    }
}

/**
 * Dropdown selector for years to allow viewing historical financial data.
 */
@Composable
fun YearSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val years = (2023..2026).toList().reversed()

    Box {
        Surface(
            onClick = { expanded = true },
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedYear.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Helper to format amounts (e.g., 1250000 -> $1.25M)
 * Connects raw numeric data from Firestore to user-friendly strings.
 */
fun formatAmount(amount: Int): String {
    return when {
        amount >= 1_000_000 -> "$${String.format("%.2f", amount / 1_000_000f)}M"
        amount >= 1_000 -> "$${String.format("%.1f", amount / 1_000f)}K"
        else -> "$$amount"
    }
}

@Composable
fun FinancialStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
fun BusinessPortfolioSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Business Portfolio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8F9FA),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE2E8F0),
                        strokeWidth = 20.dp
                    )
                    CircularProgressIndicator(
                        progress = 0.52f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF6366F1),
                        strokeWidth = 20.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Value", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                        Text("$3.48M", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PortfolioItem("Consulting", "$1.81M", "52%", Color(0xFF6366F1))
                    PortfolioItem("Investments", "$975K", "28%", Color(0xFF10B981))
                    PortfolioItem("Advisory", "$695K", "20%", Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
fun PortfolioItem(label: String, value: String, percentage: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
            }
        }
        Text(percentage, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RecentProjectsSection(
    modifier: Modifier = Modifier,
    projects: List<com.example.luminarysolutions.data.models.Project>
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (projects.isEmpty()) {
                Text("No projects found", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                projects.forEach { project ->
                    ProjectListItem(project.name, project.status, "${(project.progress * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
fun ProjectListItem(name: String, status: String, progress: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(status, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = when(status) {
                "Active" -> Color(0xFF10B981)
                "At Risk" -> Color(0xFFF43F5E)
                else -> Color.Gray
            })
        }
        LinearProgressIndicator(
            progress = { progress.replace("%", "").toFloat() / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = Color(0xFF6366F1),
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun RecentDocumentsSection(
    modifier: Modifier = Modifier,
    documents: List<com.example.luminarysolutions.data.models.Document>
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (documents.isEmpty()) {
                Text("No documents found", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                documents.forEach { doc ->
                    DocumentListItem(doc.name, doc.date)
                }
            }
        }
    }
}

@Composable
fun DocumentListItem(name: String, date: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEE2E2), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Description, null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
            }
        }
        Column {
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun InsightsCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lightbulb, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Text("Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(
                    "Strong revenue growth this quarter driven by increased consulting engagements. Focus on project execution to improve profit margins.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Content for the Performance Tab
 */
@Composable
fun PerformanceTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Performance Overview Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Performance Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("Track KPIs, targets and overall business performance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            // Date Selector
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text("Jan 1 — May 20, 2025", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                }
            }
        }

        // Metrics Row - Scrollable for mobile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PerformanceMetricCard("Overall Score", "8.7/10", "Excellent", "+12%", Icons.Default.Star, Color(0xFF6366F1))
            PerformanceMetricCard("Targets Achieved", "85%", "On Track", "+15%", Icons.Default.TrackChanges, Color(0xFF10B981))
            PerformanceMetricCard("Revenue Growth", "10.2%", "Good", "+10.2%", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF0EA5E9))
            PerformanceMetricCard("Client Satisfaction", "4.6/5", "Excellent", "+8%", Icons.Default.SentimentSatisfiedAlt, Color(0xFFF59E0B))
            PerformanceMetricCard("Efficiency Index", "78%", "On Track", "+6%", Icons.Default.Speed, Color(0xFF8B5CF6))
        }

        // Performance Score Trend
        PerformanceScoreTrendCard()

        // Performance by Category
        PerformanceByCategoryCard()

        // KPI Progress
        KPIProgressCard()

        // Quarterly Performance Comparison
        QuarterlyComparisonCard()

        // Performance Insights
        PerformanceInsightsCard()

        // Recommended Actions
        RecommendedActionsCard()
    }
}

@Composable
fun PerformanceMetricCard(label: String, value: String, subValue: String, trend: String, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.width(160.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(subValue, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Text(trend, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun PerformanceScoreTrendCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Performance Score Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View full report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(24.dp))
            // Simplified Line Chart
            Box(modifier = Modifier.height(180.dp).fillMaxWidth().padding(horizontal = 10.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points = listOf(0.6f, 0.65f, 0.7f, 0.78f, 0.82f, 0.87f)
                    val width = size.width
                    val height = size.height
                    val spacing = width / (points.size - 1)
                    
                    val path = Path().apply {
                        moveTo(0f, height * (1 - points[0]))
                        for (i in 1 until points.size) {
                            lineTo(i * spacing, height * (1 - points[i]))
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = Color(0xFF6366F1),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    
                    // Draw fill under line
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo((points.size - 1) * spacing, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(fillPath, brush = Brush.verticalGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.2f), Color.Transparent)))

                    // Draw points
                    points.forEachIndexed { i, p ->
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(i * spacing, height * (1 - p)))
                        drawCircle(Color(0xFF6366F1), radius = 4.dp.toPx(), center = Offset(i * spacing, height * (1 - p)), style = Stroke(width = 2.dp.toPx()))
                    }
                    
                    // Draw target line
                    val targetY = height * (1 - 0.8f)
                    drawLine(Color.Gray, start = Offset(0f, targetY), end = Offset(width, targetY), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                LegendItem("Performance Score", Color(0xFF6366F1))
                Spacer(Modifier.width(16.dp))
                LegendItem("Target Score (8.0)", Color.Gray)
            }
            
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Dec '24", "Jan '25", "Feb '25", "Mar '25", "Apr '25", "May '25").forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun PerformanceByCategoryCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Performance by Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryPerformanceItem("Financial Performance", 0.92f, "92%", "Excellent", Color(0xFF6366F1), Icons.Default.AccountBalance)
                CategoryPerformanceItem("Operational Efficiency", 0.78f, "78%", "Good", Color(0xFF10B981), Icons.Default.Settings)
                CategoryPerformanceItem("Client & Stakeholder", 0.88f, "88%", "Excellent", Color(0xFF8B5CF6), Icons.Default.People)
                CategoryPerformanceItem("Learning & Growth", 0.70f, "70%", "Good", Color(0xFFF59E0B), Icons.Default.School)
                CategoryPerformanceItem("Innovation & Quality", 0.75f, "75%", "Good", Color(0xFF0EA5E9), Icons.Default.Lightbulb)
            }
        }
    }
}

@Composable
fun CategoryPerformanceItem(name: String, progress: Float, value: String, rating: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
        Text(rating, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp, modifier = Modifier.width(40.dp))
    }
}

@Composable
fun KPIProgressCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("KPI Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            // KPI Table
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KPIHeaderRow()
                KPIItemRow("Revenue Growth", "10%", "10.2%", 1.02f, Color(0xFF10B981))
                KPIItemRow("Gross Profit Margin", "45%", "48.4%", 1.08f, Color(0xFF10B981))
                KPIItemRow("Project Delivery Rate", "90%", "87%", 0.97f, Color(0xFFF59E0B))
                KPIItemRow("Client Retention Rate", "85%", "88%", 1.04f, Color(0xFF10B981))
                KPIItemRow("Employee Productivity", "80%", "78%", 0.98f, Color(0xFFF43F5E))
            }
            Spacer(Modifier.height(16.dp))
            Text("View all KPIs ->", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun KPIHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("KPI", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
        Text("Target", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
        Text("Actual", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
        Text("Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun KPIItemRow(name: String, target: String, actual: String, progress: Float, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(target, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        Text(actual, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.weight(1.5f)) {
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun QuarterlyComparisonCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quarterly Performance Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View full report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                LegendItem("Q2 2024", Color(0xFFC7D2FE))
                Spacer(Modifier.width(8.dp))
                LegendItem("Q1 2025", Color(0xFF6366F1))
            }
            Spacer(Modifier.height(24.dp))
            // Bar Chart Comparison
            Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                QuarterlyBarGroup("Finance", 0.75f, 0.92f)
                QuarterlyBarGroup("Ops", 0.65f, 0.78f)
                QuarterlyBarGroup("Client", 0.80f, 0.88f)
                QuarterlyBarGroup("Growth", 0.60f, 0.70f)
                QuarterlyBarGroup("Quality", 0.68f, 0.75f)
            }
        }
    }
}

@Composable
fun QuarterlyBarGroup(label: String, val1: Float, val2: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.width(8.dp).fillMaxHeight(val1).background(Color(0xFFC7D2FE), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
            Box(modifier = Modifier.width(8.dp).fillMaxHeight(val2).background(Color(0xFF6366F1), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
    }
}

@Composable
fun PerformanceInsightsCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = Color(0xFF6366F1).copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                    }
                }
                Text("Performance Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightItem("Revenue growth is above target driven by strong consulting engagements.", Icons.Default.CheckCircle, Color(0xFF10B981))
                InsightItem("Client satisfaction improved due to better project delivery.", Icons.Default.CheckCircle, Color(0xFF10B981))
                InsightItem("Operational efficiency declined slightly due to resource constraints.", Icons.Default.Warning, Color(0xFFF59E0B))
                InsightItem("Focus area: Improve innovation initiatives in the next quarter.", Icons.Default.Info, Color(0xFF0EA5E9))
            }
        }
    }
}

@Composable
fun InsightItem(text: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
fun RecommendedActionsCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recommended Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionItem("Optimize resource allocation", "High Impact • Due in 15 days", Icons.Default.BusinessCenter, Color(0xFF6366F1))
                ActionItem("Enhance innovation programs", "Medium Impact • Due in 30 days", Icons.Default.Groups, Color(0xFF8B5CF6))
                ActionItem("Expand high-margin services", "High Impact • Due in 45 days", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF0EA5E9))
            }
        }
    }
}

@Composable
fun ActionItem(title: String, subtitle: String, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Content for the Documents Tab
 */
@Composable
fun DocumentsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Documents Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("Browse and manage all business documents", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        // Search and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search documents...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8F9FA), unfocusedBorderColor = Color.Transparent)
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA),
                modifier = Modifier.height(48.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, null, tint = Color.Gray)
                }
            }
            
            Button(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary Cards - Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DocumentSummaryCard("All Documents", "48", "Total files", Icons.Default.Folder, Color(0xFF6366F1))
            DocumentSummaryCard("Reports", "18", "125.6 MB", Icons.Default.Assessment, Color(0xFF10B981))
            DocumentSummaryCard("Financials", "12", "89.3 MB", Icons.Default.AccountBalanceWallet, Color(0xFF0EA5E9))
            DocumentSummaryCard("Contracts", "8", "45.2 MB", Icons.Default.Assignment, Color(0xFFF59E0B))
            DocumentSummaryCard("Presentations", "6", "78.4 MB", Icons.Default.PresentToAll, Color(0xFF8B5CF6))
            DocumentSummaryCard("Others", "4", "32.1 MB", Icons.Default.MoreHoriz, Color(0xFF64748B))
        }

        // Document List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DocumentRowItem("Annual Business Report 2024", "Comprehensive annual report", "Reports", "Alex Morgan", "May 18, 2025", "6.4 MB", Color(0xFFF43F5E), Icons.Default.Description)
            DocumentRowItem("Q1 2025 Financial Statement", "Financial performance Q1 2025", "Financials", "Sarah Kim", "May 15, 2025", "2.1 MB", Color(0xFF10B981), Icons.Default.TableChart)
            DocumentRowItem("Client Services Agreement", "Standard client agreement template", "Contracts", "John Doe", "May 10, 2025", "1.2 MB", Color(0xFFF59E0B), Icons.Default.Gavel)
            DocumentRowItem("Sustainability Impact Report", "ESG and sustainability initiatives", "Reports", "Emily Chen", "May 8, 2025", "4.8 MB", Color(0xFFF43F5E), Icons.Default.Description)
            DocumentRowItem("Investor Presentation Q2 2025", "Quarterly investor update deck", "Presentations", "Alex Morgan", "May 5, 2025", "12.3 MB", Color(0xFF8B5CF6), Icons.Default.Slideshow)
            DocumentRowItem("Budget vs Actual - Apr 2025", "Detailed budget analysis", "Financials", "Sarah Kim", "May 2, 2025", "1.7 MB", Color(0xFF10B981), Icons.Default.TableChart)
        }

        // Pagination
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Showing 1-10 of 48 documents", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaginationButton("<", false)
                PaginationButton("1", true)
                PaginationButton("2", false)
                PaginationButton("3", false)
                PaginationButton("...", false)
                PaginationButton("5", false)
                PaginationButton(">", false)
            }
        }
    }
}

@Composable
fun DocumentSummaryCard(title: String, count: String, subtitle: String, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.width(140.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
        }
    }
}

@Composable
fun DocumentRowItem(name: String, desc: String, category: String, uploader: String, date: String, size: String, iconColor: Color, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Surface(shape = RoundedCornerShape(8.dp), color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            
            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when(category) {
                            "Reports" -> Color(0xFFF43F5E).copy(alpha = 0.1f)
                            "Financials" -> Color(0xFF10B981).copy(alpha = 0.1f)
                            "Contracts" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            else -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when(category) {
                                "Reports" -> Color(0xFFF43F5E)
                                "Financials" -> Color(0xFF10B981)
                                "Contracts" -> Color(0xFFF59E0B)
                                else -> Color(0xFF8B5CF6)
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(size, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                }
                Text("$uploader • $date", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
            }
            
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Content for the Team Tab
 */
@Composable
fun TeamTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Team Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Team Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("Meet the leadership and team driving Luminary Solutions forward.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        // Search and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search team members...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8F9FA), unfocusedBorderColor = Color.Transparent)
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA),
                modifier = Modifier.height(48.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, null, tint = Color.Gray)
                }
            }
            
            Button(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Metrics Summary - Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TeamMetricCard("Total Members", "38", "+8%", Icons.Default.Groups, Color(0xFF6366F1))
            TeamMetricCard("Leadership Team", "8", "+12%", Icons.Default.AdminPanelSettings, Color(0xFF10B981))
            TeamMetricCard("Departments", "6", "—", Icons.Default.AccountTree, Color(0xFF0EA5E9))
            TeamMetricCard("New This Quarter", "3", "+3", Icons.Default.TrendingUp, Color(0xFFF59E0B))
            TeamMetricCard("Open Positions", "4", "+2", Icons.Default.PersonSearch, Color(0xFFF43F5E))
        }

        // Leadership Team Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Leadership Team", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("View all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LeadershipMemberCard("Alex Morgan", "Chief Executive Officer")
                LeadershipMemberCard("Sarah Kim", "Chief Financial Officer")
                LeadershipMemberCard("David Ochieng", "Chief Operations Officer")
                LeadershipMemberCard("Emily Chen", "Chief Strategy Officer")
                LeadershipMemberCard("Mark Patel", "Head of Investments")
                LeadershipMemberCard("Lisa Brown", "Head of People & Culture")
            }
        }

        // Team Members List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Team Members", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TeamMemberListItem("James Mwangi", "Consulting", "Senior Consultant", "Active", "Jan 15, 2023")
                TeamMemberListItem("Grace Wanjiku", "Finance", "Financial Analyst", "Active", "Mar 3, 2023")
                TeamMemberListItem("Kevin Zhang", "Strategy", "Strategy Manager", "Active", "Feb 20, 2023")
                TeamMemberListItem("Amina Hassan", "Investments", "Investment Associate", "Active", "Apr 10, 2023")
                TeamMemberListItem("Brian Okello", "Operations", "Operations Manager", "Active", "May 5, 2023")
                TeamMemberListItem("Nina Kapoor", "Marketing", "Marketing Specialist", "On Leave", "May 12, 2023")
            }
        }

        // Pagination
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Showing 1-8 of 38 members", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaginationButton("<", false)
                PaginationButton("1", true)
                PaginationButton("2", false)
                PaginationButton("3", false)
                PaginationButton("...", false)
                PaginationButton("5", false)
                PaginationButton(">", false)
            }
        }

        // Team Culture Section
        TeamCultureCard()
    }
}

@Composable
fun TeamMetricCard(label: String, value: String, trend: String, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.width(150.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (trend != "—") {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Text(trend, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                } else {
                    Text("—", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                }
                Text("vs last qtr", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun LeadershipMemberCard(name: String, role: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(role, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.Link, null, tint = Color(0xFF0077B5), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun TeamMemberListItem(name: String, dept: String, role: String, status: String, date: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("$role • $dept", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (status == "Active") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f)
                    ) {
                        Text(
                            status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status == "Active") Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Joined $date", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TeamCultureCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = Color(0xFF6366F1).copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Diversity1, null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text("Team Culture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("We foster a culture of collaboration, innovation, and continuous learning.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CultureMetricItem("Diversity & Inclusion", "38%", "Women in Team", Icons.Default.Groups, Color(0xFF8B5CF6))
                CultureMetricItem("Employee Satisfaction", "4.6/5", "Average Rating", Icons.Default.ThumbUp, Color(0xFF10B981))
                CultureMetricItem("Training & Development", "24", "Programs Completed", Icons.Default.School, Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
fun CultureMetricItem(label: String, value: String, subValue: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Text(subValue, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
            }
        }
    }
}

@Preview()
@Composable
fun AddProjectDialog(
    teamMembers: List<com.example.luminarysolutions.data.models.User>,
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    // State for each field in the Project model
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var spent by remember { mutableStateOf("0") }
    var status by remember { mutableStateOf("In Progress") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedLeaderIds by remember { mutableStateOf(setOf<String>()) }

    val statuses = listOf("In Progress", "Completed", "On Hold")
    var statusExpanded by remember { mutableStateOf(false) }
    var leadersExpanded by remember { mutableStateOf(false) }

    // Launcher for picking an image from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Create New Project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("Fill in project details and assign leadership", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Upload Section
                Text("Project Cover Image", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, "Upload", tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Text("Tap to upload project photo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // Primary Info Section
                Text("Primary Information", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.BusinessCenter, null, modifier = Modifier.size(18.dp)) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) }
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f))

                // Financials & Status Section
                Text("Financials & Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { if (it.all { char -> char.isDigit() }) budget = it },
                        label = { Text("Budget ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = spent,
                        onValueChange = { if (it.all { char -> char.isDigit() }) spent = it },
                        label = { Text("Spent ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Box {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Current Status") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Divider(color = Color.LightGray.copy(alpha = 0.5f))

                // Leadership Selection Section
                Text("Assign Leadership", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Box {
                    OutlinedTextField(
                        value = if (selectedLeaderIds.isEmpty()) "No leaders assigned" else "${selectedLeaderIds.size} leaders selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group Leaders") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { leadersExpanded = true }) {
                                Icon(Icons.Default.PersonAdd, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = leadersExpanded,
                        onDismissRequest = { leadersExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                    ) {
                        if (teamMembers.isEmpty()) {
                            DropdownMenuItem(text = { Text("No team members found") }, onClick = {})
                        }
                        teamMembers.forEach { member ->
                            val isSelected = selectedLeaderIds.contains(member.id)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(member.name, style = MaterialTheme.typography.bodyMedium)
                                            Text(member.role.name, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedLeaderIds = if (isSelected) {
                                        selectedLeaderIds - member.id
                                    } else {
                                        selectedLeaderIds + member.id
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Display selected leaders as Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedLeaderIds.forEach { id ->
                        val name = teamMembers.find { it.id == id }?.name ?: "Unknown"
                        AssistChip(
                            onClick = { selectedLeaderIds = selectedLeaderIds - id },
                            label = { Text(name, fontSize = 10.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val budgetInt = budget.toIntOrNull() ?: 0
                        val spentInt = spent.toIntOrNull() ?: 0
                        val progressCalc = if (budgetInt > 0) (spentInt.toFloat() / budgetInt.toFloat()).coerceIn(0f, 1f) else 0f
                        
                        onConfirm(
                            Project(
                                name = name,
                                status = status,
                                budget = budgetInt,
                                spent = spentInt,
                                progress = progressCalc,
                                lastUpdated = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                                imageUrl = selectedImageUri?.toString(), // Use the URI string
                                description = description,
                                location = location,
                                startDate = System.currentTimeMillis(),
                                volunteers = emptyList(), // Volunteers added after creation
                                groupLeaderId = selectedLeaderIds.firstOrNull() ?: "",
                                groupLeaderIds = selectedLeaderIds.toList(),
                                client = client,
                                category = category
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Project", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditProjectDialog(
    project: Project,
    teamMembers: List<com.example.luminarysolutions.data.models.User>,
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    // Initialize state with existing project data
    var name by remember { mutableStateOf(project.name) }
    var client by remember { mutableStateOf(project.client) }
    var category by remember { mutableStateOf(project.category) }
    var description by remember { mutableStateOf(project.description) }
    var location by remember { mutableStateOf(project.location) }
    var budget by remember { mutableStateOf(project.budget.toString()) }
    var spent by remember { mutableStateOf(project.spent.toString()) }
    var status by remember { mutableStateOf(project.status) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedLeaderIds by remember { mutableStateOf(project.groupLeaderIds.toSet()) }

    val statuses = listOf("In Progress", "Completed", "On Hold")
    var statusExpanded by remember { mutableStateOf(false) }
    var leadersExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Edit Project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("Update project details", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Upload Section
                Text("Project Cover Image", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (!project.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = project.imageUrl,
                                contentDescription = "Current Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, "Upload", tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Text("Tap to change photo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // Primary Info Section
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { if (it.all { char -> char.isDigit() }) budget = it },
                        label = { Text("Budget ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = spent,
                        onValueChange = { if (it.all { char -> char.isDigit() }) spent = it },
                        label = { Text("Spent ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Box {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Current Status") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Leadership Selection
                Box {
                    OutlinedTextField(
                        value = if (selectedLeaderIds.isEmpty()) "No leaders assigned" else "${selectedLeaderIds.size} leaders selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group Leaders") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { leadersExpanded = true }) {
                                Icon(Icons.Default.PersonAdd, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = leadersExpanded,
                        onDismissRequest = { leadersExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                    ) {
                        teamMembers.forEach { member ->
                            val isSelected = selectedLeaderIds.contains(member.id)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(member.name)
                                    }
                                },
                                onClick = {
                                    selectedLeaderIds = if (isSelected) {
                                        selectedLeaderIds - member.id
                                    } else {
                                        selectedLeaderIds + member.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budgetInt = budget.toIntOrNull() ?: 0
                    val spentInt = spent.toIntOrNull() ?: 0
                    val progressCalc = if (budgetInt > 0) (spentInt.toFloat() / budgetInt.toFloat()).coerceIn(0f, 1f) else 0f
                    
                    onConfirm(
                        project.copy(
                            name = name,
                            status = status,
                            budget = budgetInt,
                            spent = spentInt,
                            progress = progressCalc,
                            imageUrl = selectedImageUri?.toString() ?: project.imageUrl,
                            description = description,
                            location = location,
                            client = client,
                            category = category,
                            groupLeaderIds = selectedLeaderIds.toList(),
                            groupLeaderId = selectedLeaderIds.firstOrNull() ?: ""
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun LuminaryDetailsScreenPreview() {
    LuminarySolutionsTheme {
        LuminaryDetailsContent(
            uiState = CEODashboardUiState(
                isLoading = false
            ),
            selectedYear = 2025,
            onYearSelected = {},
            onAddProject = {},
            onDeleteProject = {},
            onUpdateProject = {},
            onBackClick = {},
            onProjectClick = {}
        )
    }
}
