package com.example.luminarysolutions.ui.team

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.*
import com.example.luminarysolutions.ui.navigation.Screen
import com.example.luminarysolutions.ui.auth.UserRole
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.text.SimpleDateFormat
import java.util.*

/**
 * Navigation destinations for the Team Dashboard.
 */
sealed class TeamTab(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : TeamTab("team_home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Projects : TeamTab("team_projects", "Projects", Icons.Outlined.WorkOutline, Icons.Filled.Work)
    object Notifications : TeamTab("team_notifications", "Alerts", Icons.Outlined.Notifications, Icons.Filled.Notifications)
    object Profile : TeamTab("team_profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

/**
 * Production-ready Team Dashboard Screen.
 * Implements MVVM with real-time Firestore integration for both Luminary and LumiSphere.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDashboardScreen(
    parentNavController: NavController,
    viewModel: TeamDashboardViewModel = viewModel()
) {
    val teamNavController = rememberNavController()
    val items = listOf(TeamTab.Home, TeamTab.Projects, TeamTab.Notifications, TeamTab.Profile)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface for error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                val navBackStackEntry by teamNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val unreadCount = if (item == TeamTab.Notifications) uiState.notifications.count { !it.isRead } else 0
                    
                    NavigationBarItem(
                        icon = { 
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) { 
                                            Text(unreadCount.toString(), style = MaterialTheme.typography.labelSmall) 
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (isSelected) item.selectedIcon else item.icon, 
                                    contentDescription = item.title
                                ) 
                            }
                        },
                        label = { Text(item.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                teamNavController.navigate(item.route) {
                                    popUpTo(teamNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            NavHost(teamNavController, startDestination = TeamTab.Home.route) {
                composable(TeamTab.Home.route) { 
                    TeamHomeContent(
                        uiState = uiState,
                        onSeeAllProjects = { teamNavController.navigate(TeamTab.Projects.route) },
                        onFreelanceClick = { id -> parentNavController.navigate(Screen.FreelanceDetails.createRoute(id)) },
                        onNgoClick = { id -> parentNavController.navigate(Screen.ProjectDetails.createRoute(id)) },
                        onTaskToggle = { viewModel.toggleTaskCompletion(it) }
                    ) 
                }
                composable(TeamTab.Projects.route) { 
                    TeamProjectsContent(
                        uiState = uiState,
                        onFreelanceClick = { parentNavController.navigate(Screen.FreelanceDetails.createRoute(it)) },
                        onNgoProjectClick = { parentNavController.navigate(Screen.ProjectDetails.createRoute(it)) }
                    ) 
                }
                composable(TeamTab.Notifications.route) { 
                    TeamNotificationsContent(
                        notifications = uiState.notifications,
                        onMarkAsRead = { viewModel.markNotificationAsRead(it) }
                    ) 
                }
                composable(TeamTab.Profile.route) { 
                    TeamProfileContent(
                        parentNavController = parentNavController,
                        profile = uiState.userProfile
                    ) 
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun TeamHomeContent(
    uiState: TeamDashboardUiState,
    onSeeAllProjects: () -> Unit,
    onFreelanceClick: (String) -> Unit,
    onNgoClick: (String) -> Unit,
    onTaskToggle: (TaskWrapper) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp, start = 20.dp, end = 20.dp)
    ) {
        item { HeaderSection(uiState.userProfile) }

        item { ProjectsStatsOverview(uiState.assignedFreelance, uiState.assignedProjects, uiState.userTasks) }

        item {
            Column {
                SectionTitle("Priority Milestones")
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.userTasks.isEmpty()) {
                    EmptyStateCard("Your task queue is clear. Good job!")
                } else {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            uiState.userTasks.take(4).forEach { wrapper ->
                                TaskItemEnhanced(wrapper, onToggle = { onTaskToggle(wrapper) })
                                if (wrapper != uiState.userTasks.take(4).last()) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionTitle("Luminary Ventures", onSeeAll = onSeeAllProjects)
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.assignedFreelance.isEmpty()) {
                    EmptyStateCard("No internal ventures assigned.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 20.dp)
                    ) {
                        items(uiState.assignedFreelance) { project ->
                            FreelanceCard(project, onClick = { onFreelanceClick(project.id) })
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionTitle("LumiSphere Programs", onSeeAll = onSeeAllProjects)
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.assignedProjects.isEmpty()) {
                    EmptyStateCard("No impact programs assigned.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 20.dp)
                    ) {
                        items(uiState.assignedProjects) { project ->
                            NgoCompactCard(project, onClick = { onNgoClick(project.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(profile: Team?) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting ✨",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = profile?.name ?: "Professional",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shadowElevation = 0.dp
        ) {
            if (!profile?.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profile?.imageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile?.name?.take(1)?.uppercase() ?: "L",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsStatsOverview(freelance: List<Freelance>, projects: List<Project>, tasks: List<TaskWrapper>) {
    val pendingTasks = tasks.count { !it.task.isDone }
    val totalEngagements = freelance.size + projects.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Open Tasks",
            count = pendingTasks.toString(),
            icon = Icons.Default.Assignment,
            colors = listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Active Works",
            count = totalEngagements.toString(),
            icon = Icons.Default.RocketLaunch,
            colors = listOf(Color(0xFF10B981), Color(0xFF34D399)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                Column {
                    Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                    Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TaskItemEnhanced(wrapper: TaskWrapper, onToggle: () -> Unit) {
    val task = wrapper.task
    val isOverdue = task.deadline < System.currentTimeMillis() && !task.isDone
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Status Indicator
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isDone) Color(0xFF10B981) 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (task.isDone) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check, 
                        contentDescription = "Done",
                        tint = Color.White, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = (if (wrapper.isFreelance) MaterialTheme.colorScheme.primary else Color(0xFF10B981)).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = wrapper.projectName.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (wrapper.isFreelance) MaterialTheme.colorScheme.primary else Color(0xFF059669),
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Text("•", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    
                    Text(
                        text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.deadline)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
            
            if (isOverdue && !task.isDone) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh, 
                            contentDescription = "Overdue",
                            tint = MaterialTheme.colorScheme.error, 
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FreelanceCard(project: Freelance, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (!project.imageUrl.isNullOrBlank()) {
                        AsyncImage(model = project.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(project.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusChip(project.status)
                Text(
                    "${(project.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NgoCompactCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    if (!project.imageUrl.isNullOrBlank()) {
                        AsyncImage(model = project.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Public, null, modifier = Modifier.padding(12.dp), tint = Color(0xFF10B981))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(project.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                }
            }
            
            Column {
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${project.volunteers.size} Volunteers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(project.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "completed" -> Color(0xFF10B981)
        "active", "ongoing", "in progress" -> Color(0xFF3B82F6)
        "pending" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp
        )
    }
}

@Composable
fun SectionTitle(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("View All", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProjectsContent(
    uiState: TeamDashboardUiState,
    onFreelanceClick: (String) -> Unit,
    onNgoProjectClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Luminary", "LumiSphere")

    val filteredFreelance = uiState.assignedFreelance.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }
    val filteredNgo = uiState.assignedProjects.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            Text("Work Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Access and manage your active project streams.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search assigned projects...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            title, 
                            fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                            color = if (selectedTab == index) {
                                if (index == 0) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Crossfade(targetState = selectedTab, label = "tab_fade") { index ->
                if (index == 0) {
                    if (filteredFreelance.isEmpty()) EmptyStateView("No Luminary ventures found")
                    else LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(filteredFreelance) { project -> DetailedProjectItem(project, onClick = { onFreelanceClick(project.id) }, isLuminary = true) }
                    }
                } else {
                    if (filteredNgo.isEmpty()) EmptyStateView("No LumiSphere programs found")
                    else LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(filteredNgo) { project -> DetailedNgoItem(project, onClick = { onNgoProjectClick(project.id) }) }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedProjectItem(project: Freelance, onClick: () -> Unit, isLuminary: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                    if (!project.imageUrl.isNullOrBlank()) AsyncImage(model = project.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Rocket, null, modifier = Modifier.padding(14.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(project.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                StatusChip(project.status)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(project.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${project.tasks.size} Milestones", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Button(onClick = onClick, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), modifier = Modifier.height(36.dp)) {
                    Text("Workspace", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailedNgoItem(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.03f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(14.dp), color = Color.White) {
                    if (!project.imageUrl.isNullOrBlank()) AsyncImage(model = project.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Public, null, modifier = Modifier.padding(14.dp), tint = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(project.category, style = MaterialTheme.typography.labelMedium, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
                StatusChip(project.status)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { project.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF10B981).copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(project.progress * 100).toInt()}% Impact Goal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(project.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${project.volunteers.size} Volunteers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(36.dp)) {
                    Text("Impact Deck", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.LayersClear, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TeamNotificationsContent(notifications: List<Notification>, onMarkAsRead: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp)) {
        Text("Alert Center", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(24.dp))
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent alerts", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notifications) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onMarkAsRead(note.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (note.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = if (note.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary) {
                                Icon(Icons.Default.Notifications, null, modifier = Modifier.padding(10.dp), tint = if (note.isRead) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(note.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamProfileContent(parentNavController: NavController, profile: Team?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 0.dp) {
            if (!profile?.imageUrl.isNullOrBlank()) AsyncImage(model = profile?.imageUrl, contentDescription = null, modifier = Modifier.clip(CircleShape), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp).padding(30.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(profile?.name ?: "Loading...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
            Text(profile?.jobtitle ?: "Team Member", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(40.dp))
        ProfileMenuItem("Personal Info", Icons.Outlined.Badge) { parentNavController.navigate(Screen.TeamSettings.createRoute("personal")) }
        ProfileMenuItem("Work Analytics", Icons.Outlined.BarChart) { parentNavController.navigate(Screen.TeamSettings.createRoute("work")) }
        ProfileMenuItem("Access & Privacy", Icons.Outlined.VpnKey) { parentNavController.navigate(Screen.TeamSettings.createRoute("security")) }
        ProfileMenuItem("App Support", Icons.Outlined.QuestionAnswer) { parentNavController.navigate(Screen.TeamSettings.createRoute("help")) }
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { FirebaseAuth.getInstance().signOut(); parentNavController.navigate("login") { popUpTo(0) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PowerSettingsNew, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Terminate Session", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 2.dp), shape = RoundedCornerShape(16.dp), color = Color.Transparent) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(18.dp))
        }
    }
}
