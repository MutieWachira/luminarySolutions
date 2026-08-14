package com.example.luminarysolutions.ui.ceo.Dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Approval
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.AddEditProjectDialog
import com.example.luminarysolutions.ui.ceo.CEODashboardUiState
import com.example.luminarysolutions.ui.ceo.CEODashboardViewModel
import com.example.luminarysolutions.ui.ceo.ProjectsViewModel
import com.example.luminarysolutions.ui.common.ExecutiveNavigationBar
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.payment.PaymentDialog
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Executive Dashboard for Luminary Solutions.
 * Designed with premium aesthetics and high-level strategic alignment in mind.
 */

// Design Tokens - Premium Palette
private val ExecutiveNavy = Color(0xFF0F172A)
private val ActionIndigo = Color(0xFF6366F1)
private val GrowthEmerald = Color(0xFF10B981)
private val RiskRose = Color(0xFFF43F5E)
private val SoftNeutral = Color(0xFFF8FAFC)
private val BorderSlate = Color(0xFFE2E8F0)

@Composable
fun CEODashboardScreen(
    navController: NavController,
    role: UserRole,
    loginViewModel: LoginViewModel,
    dashboardViewModel: CEODashboardViewModel = hiltViewModel(),
    projectsViewModel: ProjectsViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    CEODashboardContent(
        uiState = uiState,
        onLogout = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.Login.createRoute()) {
                popUpTo(0)
            }
        },
        onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
        onNavigateToDonors = { navController.navigate(Screen.Donors.route) },
        onNavigateToPartners = { navController.navigate(Screen.Partners.route) },
        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
        onNavigateToApprovals = { navController.navigate(Screen.Approvals.route) },
        onNavigateToExpenses = { navController.navigate(Screen.Expenses.createRoute()) },
        onNavigateToLuminaryDetails = { navController.navigate(Screen.LuminaryDetails.route) },
        onNavigateToLumiSphereDetails = { navController.navigate(Screen.LumiSphereDetails.route) },
        onNavigateToTeam = { navController.navigate(Screen.TeamManagement.route) },
        onAddProjectClick = { showAddProjectDialog = true },
        onPaymentClick = { showPaymentDialog = true }
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

    if (showPaymentDialog) {
        PaymentDialog(
            onDismiss = { showPaymentDialog = false }
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
    onAddProjectClick: () -> Unit,
    onPaymentClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        containerColor = SoftNeutral,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LUMISPHERE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = ExecutiveNavy
                        )
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotificationBadge(count = 3)
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(40.dp)
                                .background(RiskRose.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = RiskRose, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.White.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            ExecutiveNavigationBar(
                currentScreen = "home",
                onNavigateToHome = { /* Already here */ },
                onNavigateToProjects = onNavigateToProjects,
                onNavigateToReports = onNavigateToReports,
                onAddClick = onAddProjectClick
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ActionIndigo, strokeWidth = 4.dp)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header: Personalized Context
            ExecutiveHeader(
                name = uiState.userName,
                profileUri = selectedImageUri,
                onProfileClick = { launcher.launch("image/*") }
            )

            // Primary Metrics: High-Level Pulse
            ImpactMetricsGrid(
                uiState = uiState,
                onNavigateToProjects = onNavigateToProjects,
                onNavigateToDonors = onNavigateToDonors,
                onNavigateToExpenses = onNavigateToExpenses,
                onNavigateToPartners = onNavigateToPartners
            )

            // Strategic Portfolios: Luminary & LumiSphere
            StrategicPortfolioSection(
                uiState = uiState,
                onNavigateToLuminaryDetails = onNavigateToLuminaryDetails,
                onNavigateToLumiSphereDetails = onNavigateToLumiSphereDetails
            )

            // Dynamic Ops: Actionable Items
            OperationalControlPanel(
                onAddProjectClick = onAddProjectClick,
                onNavigateToTeam = onNavigateToTeam,
                onPaymentClick = onPaymentClick,
                onNavigateToApprovals = onNavigateToApprovals,
                onNavigateToReports = onNavigateToReports
            )

            // Initiative Pipeline
            ActiveInitiativePipeline(
                initiatives = uiState.initiatives,
                onSeeAll = onNavigateToProjects
            )

            // Critical Approvals
            ApprovalsQueue(
                approvals = uiState.recentApprovals,
                onViewAll = onNavigateToApprovals
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NotificationBadge(count: Int) {
    Surface(
        onClick = { /* Notifications */ },
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, BorderSlate),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            BadgedBox(
                badge = { 
                    Badge(
                        containerColor = RiskRose,
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) { Text(count.toString()) } 
                }
            ) {
                Icon(Icons.Outlined.Notifications, null, tint = ExecutiveNavy, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun ExecutiveHeader(
    name: String,
    profileUri: Uri?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome Back, $name 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    color = ExecutiveNavy
                )
            )
        }
        
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onProfileClick)
                .padding(4.dp) // Ring effect
                .background(ActionIndigo.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profileUri != null) {
                AsyncImage(
                    model = profileUri,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, null, tint = ActionIndigo, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun ImpactMetricsGrid(
    uiState: CEODashboardUiState,
    onNavigateToProjects: () -> Unit,
    onNavigateToDonors: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToPartners: () -> Unit
) {
    val stats = uiState.generalStats
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                label = "Active Programs",
                value = uiState.totalProgramsCount.toString(),
                trend = uiState.programsTrend,
                isPositive = uiState.isProgramsPositive,
                icon = Icons.Default.AutoGraph,
                color = ActionIndigo,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToProjects
            )
            MetricCard(
                label = "Total Funding",
                value = formatCurrency(stats.totalDonors * 15000L),
                trend = uiState.fundingTrend,
                isPositive = uiState.isFundingPositive,
                icon = Icons.Default.Payments,
                color = GrowthEmerald,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToDonors
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                label = "Operational Burn",
                value = formatCurrency(stats.totalExpenses.toLong()),
                trend = uiState.burnTrend,
                isPositive = uiState.isBurnPositive,
                icon = Icons.Default.Speed,
                color = RiskRose,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToExpenses
            )
            MetricCard(
                label = "Community Reach",
                value = formatCompact(stats.totalPartners * 250L + stats.totalDonors * 50L),
                trend = uiState.reachTrend,
                isPositive = uiState.isReachPositive,
                icon = Icons.Default.Public,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPartners
            )
        }
    }
}

@Composable
private fun StrategicPortfolioSection(
    uiState: CEODashboardUiState,
    onNavigateToLuminaryDetails: () -> Unit,
    onNavigateToLumiSphereDetails: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Strategic Portfolios",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text("Performance across business units", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            // Pager Indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) ActionIndigo else BorderSlate)
                    )
                }
            }
        }

        HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page ->
            if (page == 0) {
                PortfolioCard(
                    title = "Luminary",
                    subtitle = "COMMERCIAL OPERATIONS",
                    mainValue = formatCurrency(uiState.lumStats.totalRevenue.toLong()),
                    mainLabel = "Gross Revenue",
                    efficiency = if (uiState.lumStats.totalRevenue > 0) 
                        (uiState.lumStats.totalProfit.toFloat() / uiState.lumStats.totalRevenue.toFloat()).coerceIn(0f, 1f) 
                        else 0f,
                    efficiencyLabel = "Profit Margin",
                    color = ActionIndigo,
                    icon = Icons.Default.BusinessCenter,
                    onClick = onNavigateToLuminaryDetails
                )
            } else {
                PortfolioCard(
                    title = "LumiSphere",
                    subtitle = "SOCIAL IMPACT UNIT",
                    mainValue = uiState.lumiSphereStats.totalPrograms.toString(),
                    mainLabel = "Active Programs",
                    efficiency = uiState.lumiSphereStats.impactScore / 10f,
                    efficiencyLabel = "Impact Score",
                    color = GrowthEmerald,
                    icon = Icons.Default.Eco,
                    onClick = onNavigateToLumiSphereDetails
                )
            }
        }
    }
}

@Composable
private fun OperationalControlPanel(
    onAddProjectClick: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onPaymentClick: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Operations Control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ControlIcon(Icons.Default.AddBox, "New Project", onAddProjectClick)
            ControlIcon(Icons.Default.Groups, "HR/Team", onNavigateToTeam)
            ControlIcon(Icons.Default.AccountBalanceWallet, "Financials", onPaymentClick)
            ControlIcon(Icons.Default.FactCheck, "Approvals", onNavigateToApprovals)
            ControlIcon(Icons.Default.Insights, "Analytics", onNavigateToReports)
        }
    }
}

@Composable
private fun ActiveInitiativePipeline(
    initiatives: List<Project>,
    onSeeAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active Pipeline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onSeeAll) {
                Text("See All", color = ActionIndigo, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }

        if (initiatives.isEmpty()) {
            EmptyStateCard(message = "No active initiatives in pipeline")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                initiatives.take(3).forEach { project ->
                    PipelineItem(project = project, onClick = onSeeAll)
                }
            }
        }
    }
}

@Composable
private fun ApprovalsQueue(
    approvals: List<Approval>,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Approvals Queue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        
        if (approvals.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = GrowthEmerald, modifier = Modifier.size(32.dp))
                    Text("All requests processed.", color = ExecutiveNavy, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            approvals.forEach { approval ->
                ApprovalRequestCard(approval)
            }
        }
    }
}

// --- Component Building Blocks ---

@Composable
private fun MetricCard(
    label: String,
    value: String,
    trend: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        modifier = modifier.shadow(2.dp, RoundedCornerShape(24.dp)),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                TrendBadge(trend = trend, isPositive = isPositive)
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun TrendBadge(trend: String, isPositive: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = (if (isPositive) GrowthEmerald else RiskRose).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isPositive) GrowthEmerald else RiskRose,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isPositive) GrowthEmerald else RiskRose
            )
        }
    }
}

@Composable
private fun PortfolioCard(
    title: String,
    subtitle: String,
    mainValue: String,
    mainLabel: String,
    efficiency: Float,
    efficiencyLabel: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = ExecutiveNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            // Subtle background decoration
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
            )
            
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(subtitle, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    }
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(mainValue, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Text(mainLabel, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(efficiencyLabel, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { efficiency },
                                modifier = Modifier.size(48.dp),
                                color = color,
                                strokeWidth = 5.dp,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeCap = StrokeCap.Round
                            )
                            Text("${(efficiency * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderSlate),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = ExecutiveNavy, modifier = Modifier.size(24.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ExecutiveNavy, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PipelineItem(project: Project, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftNeutral),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.AccountTree)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = ExecutiveNavy)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = if (project.progress > 0.8f) GrowthEmerald else ActionIndigo,
                    trackColor = BorderSlate,
                    strokeCap = StrokeCap.Round
                )
            }
            Text("${(project.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ApprovalRequestCard(approval: Approval) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (approval.priority == "High") RiskRose.copy(alpha = 0.1f) else ActionIndigo.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (approval.priority == "High") Icons.Default.PriorityHigh else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (approval.priority == "High") RiskRose else ActionIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(approval.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Requested by ${approval.requestedBy}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(approval.amount.toLong()), fontWeight = FontWeight.Black, color = ExecutiveNavy)
                Text("PENDING", color = ActionIndigo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudQueue, null, modifier = Modifier.size(48.dp), tint = BorderSlate)
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ModernNavigationBar(
    onNavigateToProjects: () -> Unit,
    onNavigateToReports: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            NavigationBarItem(
                selected = true,
                onClick = { },
                icon = { Icon(Icons.Default.GridView, "Home") },
                label = { Text("Home", fontWeight = FontWeight.Bold) },
                colors = navigationItemColors()
            )
            NavigationBarItem(
                selected = false,
                onClick = onNavigateToProjects,
                icon = { Icon(Icons.Default.AccountTree, "Projects") },
                label = { Text("Pipeline") },
                colors = navigationItemColors()
            )
            
            // Central Add Action
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(ExecutiveNavy, CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                }
            }

            NavigationBarItem(
                selected = false,
                onClick = onNavigateToReports,
                icon = { Icon(Icons.Default.BarChart, "Reports") },
                label = { Text("Reports") },
                colors = navigationItemColors()
            )
            NavigationBarItem(
                selected = false,
                onClick = { },
                icon = { Icon(Icons.Default.AccountCircle, "Settings") },
                label = { Text("Profile") },
                colors = navigationItemColors()
            )
        }
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ActionIndigo,
    selectedTextColor = ActionIndigo,
    indicatorColor = ActionIndigo.copy(alpha = 0.1f),
    unselectedIconColor = Color.Gray,
    unselectedTextColor = Color.Gray
)

// Utilities
private fun formatCompact(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

private fun formatCurrency(amount: Long): String {
    return "KSh ${formatCompact(amount)}"
}

@Preview(showBackground = true)
@Composable
fun CEODashboardPreview() {
    LuminarySolutionsTheme {
        CEODashboardContent(
            uiState = CEODashboardUiState(isLoading = false),
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
