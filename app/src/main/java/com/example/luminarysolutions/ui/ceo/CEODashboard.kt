package com.example.luminarysolutions.ui.ceo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
            onSave = { name, budget, progress ->
                projectsViewModel.addProject(name, budget, progress)
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
                        title = "Projects",
                        value = stats.totalProjects.toString(),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        gradient = Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProjects
                    )
                    ModernStatCard(
                        title = "Donors",
                        value = stats.totalDonors.toString(),
                        icon = Icons.Default.Favorite,
                        gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDonors
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatCard(
                        title = "Expenses",
                        value = "KES ${stats.totalExpenses}",
                        icon = Icons.Default.AccountBalanceWallet,
                        gradient = Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48))),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpenses
                    )
                    ModernStatCard(
                        title = "Partners",
                        value = stats.totalPartners.toString(),
                        icon = Icons.Default.Handshake,
                        gradient = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPartners
                    )
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

            // Financial Summary Section
            ModernSectionCard(
                title = "Budget Utilization",
                icon = Icons.Default.PieChart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(
                        progress = { 0.65f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Budget Spent", style = MaterialTheme.typography.labelMedium)
                            Text("KES ${stats.totalExpenses}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "65%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Recent Activity Section
            ModernSectionCard(
                title = "Global Updates",
                icon = Icons.Default.History
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActivityItem("Disbursement", "KES 200k to Water Program", "2h ago", Icons.Default.Payments)
                    ActivityItem("New Partner", "Global Aid Corp joined", "5h ago", Icons.Default.PersonAdd)
                    ActivityItem("Milestone", "Phase 1 Complete", "Yesterday", Icons.Default.CheckCircle)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative background gradient circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 70.dp, y = (-30).dp)
                    .background(gradient, CircleShape, 0.1f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size(44.dp).background(gradient, RoundedCornerShape(12.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
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
fun ModernSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
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
fun ActivityItem(title: String, desc: String, time: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
