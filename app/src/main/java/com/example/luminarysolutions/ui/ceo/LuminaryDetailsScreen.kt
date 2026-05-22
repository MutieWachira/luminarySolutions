package com.example.luminarysolutions.ui.ceo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.luminarysolutions.data.models.Freelance
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
        onSearchQueryChange = { dashboardViewModel.updateSearchQuery(it) },
        onStatusFilterChange = { dashboardViewModel.updateStatusFilter(it) },
        onSortOrderChange = { dashboardViewModel.updateSortOrder(it) },
        onAddTeamMember = { dashboardViewModel.addTeamMember(it) { } },
        onDeleteTeamMember = { dashboardViewModel.deleteTeamMember(it) { } },
        onUpdateTeamMember = { dashboardViewModel.updateTeamMember(it) { } },
        onTeamSearchQueryChange = { dashboardViewModel.updateTeamSearchQuery(it) },
        onTeamStatusFilterChange = { dashboardViewModel.updateTeamStatusFilter(it) },
        onTeamSortOrderChange = { dashboardViewModel.updateTeamSortOrder(it) },
        onTeamPageChange = { dashboardViewModel.updateTeamPage(it) },
        onBackClick = { navController.popBackStack() },
        onProjectClick = { projectId ->
            navController.navigate(com.example.luminarysolutions.ui.navigation.Screen.FreelanceDetails.createRoute(projectId))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaryDetailsContent(
    uiState: CEODashboardUiState,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onAddProject: (Freelance) -> Unit,
    onDeleteProject: (String) -> Unit,
    onUpdateProject: (Freelance) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onAddTeamMember: (com.example.luminarysolutions.data.models.Team) -> Unit,
    onDeleteTeamMember: (String) -> Unit,
    onUpdateTeamMember: (com.example.luminarysolutions.data.models.Team) -> Unit,
    onTeamSearchQueryChange: (String) -> Unit,
    onTeamStatusFilterChange: (String) -> Unit,
    onTeamSortOrderChange: (String) -> Unit,
    onTeamPageChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    // Local state for tab navigation within the details screen
    var selectedTabIndex by remember { mutableIntStateOf(2) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Freelance?>(null) }
    
    var showAddTeamDialog by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<com.example.luminarysolutions.data.models.Team?>(null) }

    if (showAddProjectDialog) {
        AddFreelanceDialog(
            teamMembers = uiState.teams,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { freelance ->
                onAddProject(freelance)
                showAddProjectDialog = false
            }
        )
    }

    if (projectToEdit != null) {
        EditFreelanceDialog(
            freelance = projectToEdit!!,
            teamMembers = uiState.teams,
            onDismiss = { projectToEdit = null },
            onConfirm = { updatedFreelance ->
                onUpdateProject(updatedFreelance)
                projectToEdit = null
            }
        )
    }

    if (showAddTeamDialog) {
        AddTeamMemberDialog(
            onDismiss = { showAddTeamDialog = false },
            onConfirm = { team ->
                onAddTeamMember(team)
                showAddTeamDialog = false
            }
        )
    }

    if (teamToEdit != null) {
        EditTeamMemberDialog(
            team = teamToEdit!!,
            onDismiss = { teamToEdit = null },
            onConfirm = { updatedTeam ->
                onUpdateTeamMember(updatedTeam)
                teamToEdit = null
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
                        onProjectClick = onProjectClick,
                        onSearchQueryChange = onSearchQueryChange,
                        onStatusFilterChange = onStatusFilterChange,
                        onSortOrderChange = onSortOrderChange
                    )
                    3 -> PerformanceTabContent()
                    4 -> DocumentsTabContent(
                        uiState = uiState,
                        onPageSelected = onTeamPageChange
                    )
                    5 -> TeamTabContent(
                        uiState = uiState,
                        onAddTeamClick = { showAddTeamDialog = true },
                        onDeleteTeamMember = onDeleteTeamMember,
                        onEditTeamMember = { teamToEdit = it },
                        onSearchQueryChange = onTeamSearchQueryChange,
                        onStatusFilterChange = onTeamStatusFilterChange,
                        onSortOrderChange = onTeamSortOrderChange,
                        onPageSelected = onTeamPageChange
                    )
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
    onEditProject: (Freelance) -> Unit,
    onProjectClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit
) {
    val projects = uiState.luminaryProjects
    
    // Performance Optimization: Use remember to compute stats only when the projects list changes
    val totalProjects = remember(projects) { projects.size }
    val completedProjects = remember(projects) { projects.count { it.status == "Completed" } }
    val inProgressProjects = remember(projects) { projects.count { it.status == "In Progress" || it.status == "Active" } }
    val pendingProjects = remember(projects) { projects.count { it.status == "Pending" } }

    var projectToDelete by remember { mutableStateOf<Freelance?>(null) }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProjectStatusMiniCard(
                    label = "Active Services",
                    value = inProgressProjects.toString(),
                    trend = "+3",
                    icon = Icons.Default.Pending,
                    color = Color(0xFF0EA5E9),
                    modifier = Modifier.weight(1f)
                )
                ProjectStatusMiniCard(
                    label = "Pending Requests",
                    value = pendingProjects.toString(),
                    trend = "+5",
                    icon = Icons.Default.HourglassEmpty,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }


        // Search and Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search projects...",
                modifier = Modifier.weight(1f)
            )

            // Dedicated Search Button for consistency and accessibility
            IconButton(
                onClick = { /* Search is already reactive */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            FilterDropdown(
                text = uiState.statusFilter,
                options = listOf("All Status", "Pending", "Active", "Completed"),
                onSelected = onStatusFilterChange
            )
            FilterDropdown(
                text = "Sort: ${uiState.sortOrder}",
                options = listOf("Newest", "Oldest", "Team Size", "Applicants"),
                onSelected = onSortOrderChange
            )
            
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
                                category = project.category,
                                description = project.description,
                                dates = "Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(project.createdAt))}",
                                teamSize = project.teamIds.size,
                                clientCount = project.clientIds.size,
                                modifier = Modifier.clickable { onProjectClick(project.id) }
                            )
                        }
                    )
                }
            }
        }
        
        // Modern Interactive Pagination
        InteractivePagination(
            currentPage = 1,
            totalPages = (totalProjects / 10).coerceAtLeast(1),
            onPageSelected = {},
            modifier = Modifier.fillMaxWidth()
        )
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
fun FilterDropdown(
    text: String,
    options: List<String> = emptyList(),
    onSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
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

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.labelSmall) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DetailedProjectCard(
    name: String,
    status: String,
    imageUrl: String?,
    category: String,
    description: String,
    dates: String,
    teamSize: Int,
    clientCount: Int,
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
                // Project Image
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
                                "Active" -> Color(0xFF0EA5E9).copy(alpha = 0.1f)
                                "Completed" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "Pending" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when(status) {
                                    "Active" -> Color(0xFF0EA5E9)
                                    "Completed" -> Color(0xFF10B981)
                                    "Pending" -> Color(0xFFF59E0B)
                                    else -> Color.Gray
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(category, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
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

            // Info Row (Dates & Team & Clients)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text("Applicants: $clientCount", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

/**
 * Modern Interactive Pagination component.
 * Provides a highly responsive and animated UI for page navigation.
 * Uses Material 3 principles and optimized performance.
 */
@Composable
fun InteractivePagination(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Performance: Avoid recomposing the whole row if only the page changes
    // Using Surface for better elevation and depth
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "Page $currentPage of $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button with interactive state
                PaginationButton(
                    onClick = { if (currentPage > 1) onPageSelected(currentPage - 1) },
                    enabled = currentPage > 1,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                )

                // Visible Pages Logic - Industry standard dynamic page calculation
                val pagesToShow = remember(currentPage, totalPages) {
                    calculateVisiblePages(currentPage, totalPages)
                }
                
                pagesToShow.forEach { page ->
                    if (page == -1) {
                        Text(
                            "...", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.LightGray, 
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        val isSelected = page == currentPage
                        PageNumberButton(
                            page = page,
                            isSelected = isSelected,
                            onClick = { onPageSelected(page) }
                        )
                    }
                }
                
                // Next Button
                PaginationButton(
                    onClick = { if (currentPage < totalPages) onPageSelected(currentPage + 1) },
                    enabled = currentPage < totalPages,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                )
            }
        }
    }
}

@Composable
private fun PaginationButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: ImageVector
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) Color(0xFFF1F5F9) else Color.Transparent,
        border = if (enabled) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)) else null,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (enabled) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "pageBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Gray,
        label = "pageText"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = page.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
            )
        }
    }
}

/**
 * Helper to calculate visible page numbers for pagination.
 */
private fun calculateVisiblePages(currentPage: Int, totalPages: Int): List<Int> {
    return if (totalPages <= 5) (1..totalPages).toList()
    else {
        when {
            currentPage <= 3 -> listOf(1, 2, 3, 4, -1, totalPages)
            currentPage >= totalPages - 2 -> listOf(1, -1, totalPages - 3, totalPages - 2, totalPages - 1, totalPages)
            else -> listOf(1, -1, currentPage - 1, currentPage, currentPage + 1, -1, totalPages)
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
fun DocumentsTabContent(
    uiState: CEODashboardUiState,
    onPageSelected: (Int) -> Unit
) {
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
            SearchTextField(
                value = "",
                onValueChange = {},
                placeholder = "Search documents...",
                modifier = Modifier.weight(1f)
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

        // Modern Interactive Pagination
        InteractivePagination(
            currentPage = uiState.teamCurrentPage,
            totalPages = uiState.teamTotalPages,
            onPageSelected = onPageSelected,
            modifier = Modifier.fillMaxWidth()
        )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTabContent(
    uiState: CEODashboardUiState,
    onAddTeamClick: () -> Unit,
    onDeleteTeamMember: (String) -> Unit,
    onEditTeamMember: (com.example.luminarysolutions.data.models.Team) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onPageSelected: (Int) -> Unit
) {
    val teams = uiState.teams
    
    // Performance Optimization: Use counts from ViewModel for accuracy
    val totalMembers = uiState.totalTeamsCount
    val activeMembers = uiState.totalActiveTeamsCount
    val departmentsCount = uiState.totalDepartmentsCount

    var memberToDelete by remember { mutableStateOf<com.example.luminarysolutions.data.models.Team?>(null) }
    
    // ... (rest of the dialog logic stays same)

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Remove Team Member") },
            text = { Text("Are you sure you want to remove '${memberToDelete?.name}' from the team? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        memberToDelete?.id?.let { onDeleteTeamMember(it) }
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

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

        // Improved Search and Actions Layout
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Row 1: Search Box (Full Width for better visibility as requested)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchTextField(
                    value = uiState.teamSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Search team members by name, email or dept...",
                    modifier = Modifier.weight(1f)
                )
                
                // Add Team button in same row as search for convenience
                IconButton(
                    onClick = onAddTeamClick,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Team Member",
                        tint = Color.White
                    )
                }
            }
            
            // Row 2: Filters and Sorting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        text = "Status: ${uiState.teamStatusFilter}",
                        options = listOf("All Status", "Active", "Inactive"),
                        onSelected = onStatusFilterChange
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        text = "Sort: ${uiState.teamSortOrder}",
                        options = listOf("Newest", "Name (A-Z)", "Name (Z-A)", "Department"),
                        onSelected = onSortOrderChange
                    )
                }
            }
        }

        // Metrics Summary - Real-time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TeamMetricCard("Total Members", totalMembers.toString(), "+8%", Icons.Default.Groups, Color(0xFF6366F1))
            TeamMetricCard("Active Members", activeMembers.toString(), "+12%", Icons.Default.CheckCircle, Color(0xFF10B981))
            TeamMetricCard("Departments", departmentsCount.toString(), "—", Icons.Default.AccountTree, Color(0xFF0EA5E9))
            TeamMetricCard("Open Positions", "4", "+2", Icons.Default.PersonSearch, Color(0xFFF43F5E))
        }

        // Leadership Team Section
        var showAllLeadership by remember { mutableStateOf(false) }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Leadership Team", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "View all", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showAllLeadership = true }
                )
            }
            
            if (showAllLeadership) {
                ViewAllLeadershipDialog(
                    teams = teams.filter { it.jobtitle.contains("Chief", ignoreCase = true) || it.jobtitle.contains("Head", ignoreCase = true) || it.role == com.example.luminarysolutions.ui.auth.UserRole.CEO },
                    onDismiss = { showAllLeadership = false },
                    onEdit = onEditTeamMember,
                    onDelete = { memberToDelete = it }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val leadership = teams.filter { it.jobtitle.contains("Chief", ignoreCase = true) || it.jobtitle.contains("Head", ignoreCase = true) || it.role == com.example.luminarysolutions.ui.auth.UserRole.CEO }
                
                if (leadership.isEmpty()) {
                    // Placeholder if no leadership found
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("No leadership members identified.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                } else {
                    leadership.forEach { member ->
                        LeadershipMemberCard(
                            member = member,
                            onEdit = { onEditTeamMember(member) },
                            onDelete = { memberToDelete = member }
                        )
                    }
                }
            }
        }

        // Team Members List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Team Members", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (teams.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No team members found", color = Color.Gray)
                    }
                } else {
                    teams.forEach { member ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                when (value) {
                                    SwipeToDismissBoxValue.EndToStart -> memberToDelete = member
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        onEditTeamMember(member)
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
                                TeamMemberListItem(
                                    name = member.name,
                                    dept = member.department,
                                    role = member.jobtitle,
                                    status = if (member.enabled) "Active" else "Inactive",
                                    date = "Jan 2026", // Mock date as it's not in Team model
                                    imageUrl = member.imageUrl
                                )
                            }
                        )
                    }
                }
            }
        }

        // Modern Interactive Pagination
        InteractivePagination(
            currentPage = uiState.teamCurrentPage,
            totalPages = uiState.teamTotalPages,
            onPageSelected = onPageSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Team Culture Section
        TeamCultureCard(uiState.culture)
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
fun LeadershipMemberCard(
    member: com.example.luminarysolutions.data.models.Team,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
                
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(64.dp).align(Alignment.Center)
                ) {
                    if (!member.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = member.imageUrl,
                            contentDescription = member.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(member.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(member.jobtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Link, null, tint = Color(0xFF0077B5), modifier = Modifier.size(14.dp))
                if (member.gender.isNotBlank()) {
                    Text(member.gender.take(1).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun TeamMemberListItem(name: String, dept: String, role: String, status: String, date: String, imageUrl: String? = null) {
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
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Member Image",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
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
fun TeamCultureCard(culture: com.example.luminarysolutions.data.models.TeamCulture) {
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
                CultureMetricItem("Diversity & Inclusion", culture.diversityRate, "Women in Team", Icons.Default.Groups, Color(0xFF8B5CF6))
                CultureMetricItem("Employee Satisfaction", culture.satisfactionScore, "Average Rating", Icons.Default.ThumbUp, Color(0xFF10B981))
                CultureMetricItem("Training & Development", culture.trainingPrograms.toString(), "Programs Completed", Icons.Default.School, Color(0xFFF59E0B))
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

@Composable
fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.example.luminarysolutions.data.models.Team) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Consulting") }
    var jobtitle by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    // Security/Business Rule: Default role is always TEAM for this entry point
    val role = com.example.luminarysolutions.ui.auth.UserRole.TEAM
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val departments = listOf("Consulting", "Investment", "Advisory", "Strategy", "Tech", "Operations", "Finance", "Legal")
    
    var deptExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Team Member", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Upload
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Surface(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(80.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.padding(24.dp))
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                
                // Gender Selection (Radio Buttons)
                Column {
                    Text("Gender", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                        Text("Male", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                        Text("Female", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(value = jobtitle, onValueChange = { jobtitle = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { deptExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                        departments.forEach { dept ->
                            DropdownMenuItem(text = { Text(dept) }, onClick = { department = dept; deptExpanded = false })
                        }
                    }
                }
                // App Role selection removed as per security/business requirements
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onConfirm(com.example.luminarysolutions.data.models.Team(
                            name = name,
                            email = email,
                            phone = phone,
                            department = department,
                            jobtitle = jobtitle,
                            gender = gender,
                            role = role, // Automatically set to UserRole.TEAM
                            imageUrl = selectedImageUri?.toString(),
                            enabled = true
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Invitation", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditTeamMemberDialog(
    team: com.example.luminarysolutions.data.models.Team,
    onDismiss: () -> Unit,
    onConfirm: (com.example.luminarysolutions.data.models.Team) -> Unit
) {
    var name by remember { mutableStateOf(team.name) }
    var email by remember { mutableStateOf(team.email) }
    var phone by remember { mutableStateOf(team.phone) }
    var department by remember { mutableStateOf(team.department) }
    var jobtitle by remember { mutableStateOf(team.jobtitle) }
    var gender by remember { mutableStateOf(team.gender.ifBlank { "Male" }) }
    // Role is preserved from the existing team member, selection removed from UI
    var enabled by remember { mutableStateOf(team.enabled) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val departments = listOf("Consulting", "Investment", "Advisory", "Strategy", "Tech", "Operations", "Finance", "Legal")
    
    var deptExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Team Member", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Surface(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(80.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (!team.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = team.imageUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.padding(24.dp))
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                
                // Gender Selection (Radio Buttons)
                Column {
                    Text("Gender", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                        Text("Male", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                        Text("Female", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(value = jobtitle, onValueChange = { jobtitle = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { deptExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                        departments.forEach { dept ->
                            DropdownMenuItem(text = { Text(dept) }, onClick = { department = dept; deptExpanded = false })
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Account Enabled", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(team.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        department = department,
                        jobtitle = jobtitle,
                        gender = gender,
                        imageUrl = selectedImageUri?.toString() ?: team.imageUrl,
                        enabled = enabled
                    ))
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

@Composable
fun AddFreelanceDialog(
    teamMembers: List<com.example.luminarysolutions.data.models.Team>,
    onDismiss: () -> Unit,
    onConfirm: (Freelance) -> Unit
) {
    // State for each field in the Freelance model
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Consulting") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedTeamIds by remember { mutableStateOf(setOf<String>()) }

    val statuses = listOf("Pending", "Active", "Completed")
    val categories = listOf("Consulting", "Investment", "Advisory", "Strategy", "Tech", "Other")
    var statusExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var teamExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Create New Freelance Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("Fill in service details and assign team", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                Text("Service Cover Image", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                                Text("Tap to upload photo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // Primary Info Section
                Text("Primary Information", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

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

                // Team Assignment
                Text("Assign Team Members", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Box {
                    OutlinedTextField(
                        value = if (selectedTeamIds.isEmpty()) "No members assigned" else "${selectedTeamIds.size} members selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Team") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { teamExpanded = true }) {
                                Icon(Icons.Default.PersonAdd, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = teamExpanded,
                        onDismissRequest = { teamExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                    ) {
                        teamMembers.forEach { member ->
                            val isSelected = selectedTeamIds.contains(member.id)
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
                                    selectedTeamIds = if (isSelected) {
                                        selectedTeamIds - member.id
                                    } else {
                                        selectedTeamIds + member.id
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
                    if (name.isNotBlank()) {
                        onConfirm(
                            Freelance(
                                name = name,
                                status = status,
                                imageUrl = selectedImageUri?.toString(),
                                description = description,
                                category = category,
                                teamIds = selectedTeamIds.toList(),
                                clientIds = emptyList() // Start with no applicants
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Freelance Service", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditFreelanceDialog(
    freelance: Freelance,
    teamMembers: List<com.example.luminarysolutions.data.models.Team>,
    onDismiss: () -> Unit,
    onConfirm: (Freelance) -> Unit
) {
    var name by remember { mutableStateOf(freelance.name) }
    var category by remember { mutableStateOf(freelance.category) }
    var description by remember { mutableStateOf(freelance.description) }
    var status by remember { mutableStateOf(freelance.status) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedTeamIds by remember { mutableStateOf(freelance.teamIds.toSet()) }

    val statuses = listOf("Pending", "Active", "Completed")
    val categories = listOf("Consulting", "Investment", "Advisory", "Strategy", "Tech", "Other")
    var statusExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var teamExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Freelance Service") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Box {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
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
                
                Box {
                    OutlinedTextField(
                        value = if (selectedTeamIds.isEmpty()) "No members assigned" else "${selectedTeamIds.size} members selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Team") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { teamExpanded = true }) {
                                Icon(Icons.Default.PersonAdd, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = teamExpanded,
                        onDismissRequest = { teamExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                    ) {
                        teamMembers.forEach { member ->
                            val isSelected = selectedTeamIds.contains(member.id)
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
                                    selectedTeamIds = if (isSelected) {
                                        selectedTeamIds - member.id
                                    } else {
                                        selectedTeamIds + member.id
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
                    onConfirm(
                        freelance.copy(
                            name = name,
                            category = category,
                            description = description,
                            status = status,
                            imageUrl = selectedImageUri?.toString() ?: freelance.imageUrl,
                            teamIds = selectedTeamIds.toList()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
/**
 * Modern search input field with improved visibility and interaction.
 * Follows industry best practices for accessibility and clean UI.
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { 
            Text(
                text = placeholder, 
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f)
            ) 
        },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Search, 
                contentDescription = "Search Icon", 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            ) 
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close, 
                        contentDescription = "Clear Search", 
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8F9FA),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun ViewAllLeadershipDialog(
    teams: List<com.example.luminarysolutions.data.models.Team>,
    onDismiss: () -> Unit,
    onEdit: (com.example.luminarysolutions.data.models.Team) -> Unit,
    onDelete: (com.example.luminarysolutions.data.models.Team) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leadership Team", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                teams.forEach { member ->
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
                                if (!member.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = member.imageUrl,
                                        contentDescription = member.name,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(member.jobtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                            }
                            Row {
                                IconButton(onClick = { onEdit(member); onDismiss() }) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                }
                                IconButton(onClick = { onDelete(member); onDismiss() }) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
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
            onSearchQueryChange = {},
            onStatusFilterChange = {},
            onSortOrderChange = {},
            onAddTeamMember = {},
            onDeleteTeamMember = {},
            onUpdateTeamMember = {},
            onTeamSearchQueryChange = {},
            onTeamStatusFilterChange = {},
            onTeamSortOrderChange = {},
            onTeamPageChange = {},
            onBackClick = {},
            onProjectClick = {}
        )
    }
}
