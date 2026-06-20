package com.example.luminarysolutions.ui.ceo.Dashboard

import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.AddEditProjectDialog
import com.example.luminarysolutions.ui.ceo.CEODashboardUiState
import com.example.luminarysolutions.ui.ceo.CEODashboardViewModel
import com.example.luminarysolutions.ui.ceo.ProjectsViewModel
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CEODashboardScreen(
    navController: NavController,
    role: UserRole,
    loginViewModel: LoginViewModel,
    dashboardViewModel: CEODashboardViewModel = viewModel(),
    projectsViewModel: ProjectsViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    var showAddProjectDialog by remember { mutableStateOf(false) }

    CEODashboardContent(
        uiState = uiState,
        onLogout = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        },
        onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
        onNavigateToDonors = { navController.navigate(Screen.Donors.route) },
        onNavigateToPartners = { navController.navigate(Screen.Partners.route) },
        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
        onNavigateToApprovals = { navController.navigate(Screen.Approvals.route) },
        onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
        onNavigateToLuminaryDetails = { navController.navigate(Screen.LuminaryDetails.route) },
        onNavigateToLumiSphereDetails = { navController.navigate(Screen.LumiSphereDetails.route) },
        onNavigateToTeam = { navController.navigate(Screen.TeamManagement.route) },
        onAddProjectClick = { showAddProjectDialog = true }
    )

    if (showAddProjectDialog) {
        AddEditProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSave = { project, uri ->
                projectsViewModel.addProject(project, uri)
                showAddProjectDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CEODashboardContent(
    uiState: CEODashboardUiState,
    onLogout: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToDonors: () -> Unit,
    onNavigateToPartners: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToLuminaryDetails: () -> Unit,
    onNavigateToLumiSphereDetails: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onAddProjectClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        containerColor = Color(0xFFFBFBFE), // Soft premium background
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LUMISPHERE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            color = Color(0xFF111827)
                        )
                    )
                },
                actions = {
                    Surface(
                        onClick = { /* Notifications */ },
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 2.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            BadgedBox(badge = { Badge(containerColor = Color(0xFFF43F5E)) { Text("3", color = Color.White) } }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF374151)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color(0xFFF43F5E))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(80.dp)
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                        label = { Text("Home", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6366F1),
                            selectedTextColor = Color(0xFF6366F1),
                            indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                            unselectedIconColor = Color(0xFF9CA3AF),
                            unselectedTextColor = Color(0xFF9CA3AF)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToProjects,
                        icon = { Icon(Icons.Default.AccountTree, "Projects") },
                        label = { Text("Projects") }
                    )
                    
                    // Floating Center Action
                    Box(
                        modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = onAddProjectClick,
                            shape = CircleShape,
                            color = Color(0xFF111827),
                            shadowElevation = 12.dp,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToReports,
                        icon = { Icon(Icons.Default.BarChart, "Reports") },
                        label = { Text("Reports") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { Icon(Icons.Default.Settings, "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6366F1))
            }
            return@Scaffold
        }

        val stats = uiState.generalStats
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Executive Welcome Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Executive",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.size(56.dp).clickable { launcher.launch("image/*") },
                    shadowElevation = 2.dp
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "CEO Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.background(Color(0xFFF3F4F6))) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF9CA3AF))
                        }
                    }
                }
            }

            // High-Level Impact Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumStatCard(
                        title = "Total Programs",
                        value = uiState.totalProgramsCount.toString(),
                        trend = "+12%",
                        icon = Icons.Default.RocketLaunch,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProjects
                    )
                    PremiumStatCard(
                        title = "Total Funding",
                        value = "KSh ${formatCompact(stats.totalDonors * 15000L)}",
                        trend = "+8.4%",
                        icon = Icons.Default.AccountBalance,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDonors
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumStatCard(
                        title = "Operational Cost",
                        value = "KSh ${formatCompact(stats.totalExpenses.toLong())}",
                        trend = "-2.1%",
                        icon = Icons.Default.Wallet,
                        color = Color(0xFFF43F5E),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpenses
                    )
                    PremiumStatCard(
                        title = "Impact Reach",
                        value = formatCompact(stats.totalPartners * 250L + stats.totalDonors * 50L),
                        trend = "+18%",
                        icon = Icons.Default.Public,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPartners
                    )
                }
            }

            // Brand Strategic View
            val pagerState = rememberPagerState(pageCount = { 2 })
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Business Units", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("Performance across Luminary & LumiSphere", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 20.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == index) Color(0xFF6366F1) else Color(0xFFE5E7EB))
                            )
                        }
                    }
                }
                
                HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page ->
                    if (page == 0) {
                        BrandStrategyCard(
                            name = "Luminary",
                            type = "BUSINESS UNIT",
                            mainMetric = formatCurrency(uiState.lumStats.totalRevenue.toLong()),
                            metricLabel = "Annual Revenue",
                            progress = if (uiState.lumStats.totalRevenue > 0) 
                                (uiState.lumStats.totalProfit.toFloat() / uiState.lumStats.totalRevenue.toFloat()).coerceIn(0f, 1f) 
                                else 0.78f,
                            accentColor = Color(0xFF6366F1),
                            icon = Icons.Default.BusinessCenter,
                            onDetailsClick = onNavigateToLuminaryDetails
                        )
                    } else {
                        BrandStrategyCard(
                            name = "LumiSphere",
                            type = "NON-PROFIT UNIT",
                            mainMetric = uiState.lumiSphereStats.totalPrograms.toString(),
                            metricLabel = "Active Programs",
                            progress = uiState.lumiSphereStats.impactScore / 10f,
                            accentColor = Color(0xFF10B981),
                            icon = Icons.Default.AutoAwesome,
                            onDetailsClick = onNavigateToLumiSphereDetails
                        )
                    }
                }
            }

            // Quick Operations
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Operations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuickActionItem(Icons.Default.LibraryAdd, "Project", onAddProjectClick)
                    QuickActionItem(Icons.Default.GroupAdd, "Staff", onNavigateToTeam)
                    QuickActionItem(Icons.Default.AssignmentTurnedIn, "Approvals", onNavigateToApprovals)
                    QuickActionItem(Icons.Default.Analytics, "Insights", onNavigateToReports)
                }
            }

            // Ongoing Initiatives
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Top Initiatives", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("See all", color = Color(0xFF6366F1), style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onNavigateToProjects() })
                }
                
                if (uiState.initiatives.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                    ) {
                        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, null, modifier = Modifier.size(48.dp), tint = Color(0xFFD1D5DB))
                            Text("No active initiatives found", color = Color(0xFF9CA3AF))
                        }
                    }
                } else {
                    uiState.initiatives.take(3).forEach { project ->
                        InitiativeRowItem(
                            project = project,
                            onClick = { onNavigateToProjects() }
                        )
                    }
                }
            }

            // Approvals Queue
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Pending Approvals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                
                if (uiState.recentApprovals.isEmpty()) {
                    Text("All clear! No pending tasks.", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodyMedium)
                } else {
                    uiState.recentApprovals.forEach { approval ->
                        ApprovalCompactCard(approval)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PremiumStatCard(
    title: String,
    value: String,
    trend: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        modifier = modifier,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp), 
                    color = color.copy(alpha = 0.1f), 
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { 
                        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) 
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp), 
                    color = if (trend.startsWith("+")) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF43F5E).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (trend.startsWith("+")) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = trend.removePrefix("+").removePrefix("-"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column {
                Text(
                    text = value, 
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF111827)
                )
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelMedium, 
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BrandStrategyCard(
    name: String,
    type: String,
    mainMetric: String,
    metricLabel: String,
    progress: Float,
    accentColor: Color,
    icon: ImageVector,
    onDetailsClick: () -> Unit
) {
    Surface(
        onClick = onDetailsClick,
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827), // Modern dark theme for brand cards
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = accentColor, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    }
                    Column {
                        Text(name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(type, color = accentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Icon(Icons.Default.ArrowForward, null, tint = Color.White.copy(alpha = 0.5f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(mainMetric, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(metricLabel, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Overall Performance", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(44.dp),
                        color = accentColor,
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.size(68.dp),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = Color(0xFF111827), modifier = Modifier.size(28.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
    }
}

@Composable
fun InitiativeRowItem(project: Project, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp), 
                modifier = Modifier.size(64.dp),
                color = Color(0xFFF3F4F6)
            ) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = "Project Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Business),
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Business)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = project.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { project.progress },
                        modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                        color = when {
                            project.status == "At Risk" -> Color(0xFFF43F5E)
                            project.progress >= 0.8f -> Color(0xFF10B981)
                            else -> Color(0xFF6366F1)
                        },
                        trackColor = Color(0xFFF3F4F6)
                    )
                    Text("${(project.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Text(
                    text = "Last updated: ${project.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFD1D5DB))
        }
    }
}

@Composable
fun ApprovalCompactCard(approval: Approval) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = CircleShape,
                color = when(approval.priority) {
                    "High" -> Color(0xFFF43F5E).copy(alpha = 0.1f)
                    else -> Color(0xFF6366F1).copy(alpha = 0.1f)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (approval.priority == "High") Icons.Default.PriorityHigh else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (approval.priority == "High") Color(0xFFF43F5E) else Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(approval.type, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(approval.requestedBy, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("KSh ${formatCompact(approval.amount.toLong())}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
        }
    }
}


data class BrandMetric(
    val label: String,
    val value: String,
    val trend: String
)

@Composable
fun BrandOverviewCard(
    brandName: String,
    brandType: String,
    overviewTitle: String,
    metrics: List<BrandMetric>,
    chartProgress: Float,
    chartColor: Color,
    centerIcon: ImageVector,
    modifier: Modifier = Modifier,
    onViewDetailsClick: () -> Unit = {}
) {
    Surface(
        onClick = onViewDetailsClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF111418),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = brandName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = overviewTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = brandType,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    metrics.forEach { metric ->
                        Column {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = metric.value,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (metric.trend.startsWith("+")) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (metric.trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = metric.trend,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (metric.trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = chartColor.copy(alpha = 0.1f),
                        strokeWidth = 16.dp,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { chartProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = chartColor,
                        strokeWidth = 16.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        imageVector = centerIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View $brandName Details",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CircularMetric(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(80.dp),
            color = color,
            strokeWidth = 8.dp,
            trackColor = color.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    trend: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trend.startsWith("+")) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " vs last month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ModernSectionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp))
            }
            content()
        }
    }
}

fun formatCompact(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

fun formatCurrency(amount: Long): String {
    return "KSh ${formatCompact(amount)}"
}

@Composable
fun ActivityItem(title: String, desc: String, time: String, icon: ImageVector, iconColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconColor)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

data class Initiative(
    val title: String,
    val progress: Float,
    val status: String,
    val statusColor: Color,
    val brand: String,
    val brandColor: Color,
    val imageUrl: String
)

@Composable
fun InitiativeCard(initiative: Initiative, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(initiative.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = initiative.brandColor
                ) {
                    Text(
                        text = initiative.brand,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = initiative.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LinearProgressIndicator(
                        progress = { initiative.progress },
                        modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                        color = initiative.brandColor,
                        trackColor = initiative.brandColor.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "${(initiative.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(initiative.statusColor))
                    Text(
                        text = initiative.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CEODashboardPreview() {
    LuminarySolutionsTheme {
        CEODashboardContent(
            uiState = CEODashboardUiState(
                isLoading = false
            ),
            onLogout = {},
            onNavigateToProjects = {},
            onNavigateToDonors = {},
            onNavigateToPartners = {},
            onNavigateToReports = {},
            onNavigateToApprovals = {},
            onNavigateToExpenses = {},
            onNavigateToLuminaryDetails = {},
            onNavigateToLumiSphereDetails = {},
            onNavigateToTeam = {},
            onAddProjectClick = {}
        )
    }
}
