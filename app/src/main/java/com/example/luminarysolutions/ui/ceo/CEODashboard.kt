package com.example.luminarysolutions.ui.ceo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import coil.compose.rememberAsyncImagePainter
import com.example.luminarysolutions.data.firebase.DashboardStats
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Screen
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
        onAddProjectClick = { showAddProjectDialog = true }
    )

    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSave = { name, budget, description, location, startDate, imageUrl, imageUri ->
                projectsViewModel.addProject(name, budget, description, location, startDate, imageUrl, imageUri)
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LUMISPHERE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color.Black.copy(alpha = 0.05f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProjects,
                    icon = { Icon(Icons.Default.BusinessCenter, "Projects") },
                    label = { Text("Projects") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color.Black.copy(alpha = 0.05f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = onAddProjectClick,
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, "Quick Action")
                    }
                }
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToReports,
                    icon = { Icon(Icons.Default.Assessment, "Reports") },
                    label = { Text("Reports") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color.Black.copy(alpha = 0.05f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color.Black.copy(alpha = 0.05f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            return@Scaffold
        }

        val stats = uiState.stats

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "Executive Overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { launcher.launch("image/*") },
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "CEO Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "CEO",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        // Small edit overlay icon indicator
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(bottom = 4.dp),
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatCard(
                        title = "Total Projects",
                        value = stats.totalProjects.toString(),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconColor = Color(0xFF6366F1),
                        trend = "+12%",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProjects
                    )
                    ModernStatCard(
                        title = "Total Funds",
                        value = "$1.48M",
                        icon = Icons.Default.Payments,
                        iconColor = Color(0xFF10B981),
                        trend = "+8.5%",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDonors
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatCard(
                        title = "Total Expenses",
                        value = "$654K",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFFF43F5E),
                        trend = "-4.2%",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpenses
                    )
                    ModernStatCard(
                        title = "Impact Reach",
                        value = "12,430",
                        icon = Icons.Default.Group,
                        iconColor = Color(0xFFF59E0B),
                        trend = "+15%",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPartners
                    )
                }
            }

            // Brand Performance Section
            val pagerState = rememberPagerState(pageCount = { 2 })
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSpacing = 16.dp
                ) { page ->
                    if (page == 0) {
                        BrandOverviewCard(
                            brandName = "Luminary",
                            brandType = "Business",
                            overviewTitle = "Financial Overview",
                            metrics = listOf(
                                BrandMetric("Revenue", "$1.25M", "+10.3%"),
                                BrandMetric("Expenses", "$645K", "+4.2%"),
                                BrandMetric("Profit", "$605K", "+18.7%")
                            ),
                            chartProgress = 0.75f,
                            chartColor = Color(0xFF6366F1),
                            centerIcon = Icons.Default.BusinessCenter,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        BrandOverviewCard(
                            brandName = "Lumisphere",
                            brandType = "NGO",
                            overviewTitle = "Impact Overview",
                            metrics = listOf(
                                BrandMetric("Total Donations", "$870K", "+14.5%"),
                                BrandMetric("Programs Funded", "18", "+2"),
                                BrandMetric("Beneficiaries", "12,430", "+10%")
                            ),
                            chartProgress = 0.65f,
                            chartColor = Color(0xFF10B981),
                            centerIcon = Icons.Default.Favorite,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Pager Indicator
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                                .height(8.dp)
                        )
                    }
                }
            }

            // Quick Actions
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Operational Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(Icons.Default.Add, "Project", onAddProjectClick)
                    QuickActionButton(Icons.Default.Assessment, "Reports") {
                        onNavigateToReports()
                    }
                    QuickActionButton(Icons.AutoMirrored.Filled.FactCheck, "Approvals") {
                        onNavigateToApprovals()
                    }
                    QuickActionButton(Icons.Default.Security, "Audit") {
                        // Navigate to logs
                    }
                }
            }

            // Ongoing Initiatives Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ongoing Initiatives",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToProjects() }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    val initiatives = listOf(
                        Initiative(
                            "Corporate Office Redesign",
                            0.75f,
                            "On Track",
                            Color(0xFF10B981),
                            "LUMINARY",
                            Color(0xFF6366F1),
                            "https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=2069"
                        ),
                        Initiative(
                            "Brand Strategy Consulting",
                            0.40f,
                            "At Risk",
                            Color(0xFFF59E0B),
                            "LUMINARY",
                            Color(0xFF6366F1),
                            "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?q=80&w=2070"
                        ),
                        Initiative(
                            "Education For All Campaign",
                            0.85f,
                            "On Track",
                            Color(0xFF10B981),
                            "LUMISPHERE",
                            Color(0xFF10B981),
                            "https://images.unsplash.com/photo-1509062522246-3755977927d7?q=80&w=2104"
                        ),
                        Initiative(
                            "Community Health Initiative",
                            0.20f,
                            "Needs Attention",
                            Color(0xFFF43F5E),
                            "LUMISPHERE",
                            Color(0xFF10B981),
                            "https://images.unsplash.com/photo-1576091160550-2173dba999ef?q=80&w=2070"
                        )
                    )
                    items(initiatives) { initiative ->
                        InitiativeCard(initiative)
                    }
                }
            }

            // Alerts & Approvals Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alerts & Approvals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToApprovals() }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ActivityItem(
                    title = "Budget approval needed for Project Nexa",
                    desc = "Luminary • Requested by Alex Morgan",
                    time = "2h ago",
                    icon = Icons.Default.PendingActions,
                    iconColor = Color(0xFFF59E0B)
                )
                ActivityItem(
                    title = "New donation of $10,000 received",
                    desc = "Lumisphere • From Global Future Foundation",
                    time = "5h ago",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF10B981)
                )
                ActivityItem(
                    title = "Expense limit exceeded in Outreach Program",
                    desc = "Lumisphere • Program ID: LS-2045",
                    time = "9h ago",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFF43F5E)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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
    modifier: Modifier = Modifier
) {
    Surface(
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
    CEODashboardContent(
        uiState = CEODashboardUiState(stats = DashboardStats(12, 85, 450000)),
        onLogout = {},
        onNavigateToProjects = {},
        onNavigateToDonors = {},
        onNavigateToPartners = {},
        onNavigateToReports = {},
        onNavigateToApprovals = {},
        onNavigateToExpenses = {},
        onAddProjectClick = {}
    )
}
