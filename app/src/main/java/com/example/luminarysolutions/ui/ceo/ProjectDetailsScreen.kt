package com.example.luminarysolutions.ui.ceo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.auth.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
            LargeTopAppBar(
                title = { 
                    Text(
                        uiState.project?.name ?: "Project Details", 
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.error ?: "Unknown error", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                uiState.project?.let { project ->
                    ProjectDetailsContent(
                        project = project,
                        volunteers = uiState.volunteers,
                        applications = uiState.volunteerApplications,
                        teamMembers = uiState.teamMembers,
                        isSaving = uiState.isSaving,
                        onExpensesClick = {
                            navController.navigate(com.example.luminarysolutions.ui.navigation.Screen.Expenses.createRoute(project.id))
                        },
                        onToggleTask = { taskId, isDone ->
                            viewModel.toggleTaskStatus(project.id, taskId, isDone)
                        },
                        onAssignLeader = { leaderId ->
                            viewModel.assignGroupLeader(project.id, leaderId)
                        },
                        onAddTask = { title, desc, ids, names, type, dl ->
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            viewModel.addTask(project.id, title, desc, ids, names, currentUserId, type, dl)
                        },
                        onUpdateTask = { viewModel.updateTask(project.id, it) },
                        onDeleteTask = { viewModel.deleteTask(project.id, it) },
                        onVolunteerClick = {
                            navController.navigate("volunteer_signup/${project.id}")
                        },
                        onDonateClick = {
                            navController.navigate("donation/${project.id}")
                        },
                        onApproveVolunteer = { viewModel.approveVolunteer(it) },
                        onRejectVolunteer = { viewModel.rejectVolunteer(it) },
                        onDeleteVolunteer = { viewModel.deleteVolunteer(projectId, it) },
                        onEditVolunteer = { viewModel.updateVolunteer(it) },
                        onUpdateTeamMembers = { projId, members -> 
                            viewModel.updateTeamMembers(projId, members)
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
    applications: List<Volunteer>,
    teamMembers: List<User>,
    isSaving: Boolean,
    onExpensesClick: () -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    onAssignLeader: (String) -> Unit,
    onAddTask: (String, String, List<String>, List<String>, com.example.luminarysolutions.data.models.AssigneeType, Long) -> Unit,
    onUpdateTask: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onVolunteerClick: () -> Unit,
    onDonateClick: () -> Unit,
    onApproveVolunteer: (String) -> Unit,
    onRejectVolunteer: (String) -> Unit,
    onDeleteVolunteer: (String) -> Unit,
    onEditVolunteer: (Volunteer) -> Unit,
    onUpdateTeamMembers: (String, List<String>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Volunteers", "Team", "Finance", "Tasks")
    val scrollState = rememberScrollState()
    
    var volunteerToDelete by remember { mutableStateOf<String?>(null) }
    var volunteerToEdit by remember { mutableStateOf<Volunteer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // Hero Section
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            if (!project.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text(
                        project.category.ifBlank { "Impact" }.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                ProjectStatusBadgeLocal(project.status)
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(project.location.ifBlank { "Operational HQ" }, style = MaterialTheme.typography.labelMedium)
                }
                val startDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(project.startDate)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(startDateStr, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(label = "Volunteers", value = project.volunteers.size.toString(), icon = Icons.Default.Groups, modifier = Modifier.weight(1f))
                QuickStatCard(label = "Staff", value = project.teamMemberIds.size.toString(), icon = Icons.Default.Badge, modifier = Modifier.weight(1f))
                QuickStatCard(label = "Tasks", value = project.tasks.size.toString(), icon = Icons.Default.Assignment, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            ModernDetailCard(title = "Execution & Impact", icon = Icons.Default.Insights) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${(project.progress * 100).roundToInt()}% Goal Reached", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(if (project.status == "At Risk") "Requires Attention" else "On Track", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (project.status == "At Risk") MaterialTheme.colorScheme.error else Color(0xFF10B981))
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { project.progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = getProjectStatusColorLocal(project.status),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (selectedTab) {
                0 -> OverviewTab(project, teamMembers, volunteers)
                1 -> VolunteersTab(volunteers, applications, onApproveVolunteer, onRejectVolunteer, { volunteerToDelete = it }, { volunteerToEdit = it })
                2 -> TeamTab(project, teamMembers, onUpdateTeamMembers)
                3 -> FinanceTab(project, onExpensesClick)
                4 -> ActivityTab(project, volunteers, teamMembers, isSaving, onToggleTask, onAssignLeader, onAddTask, onUpdateTask, onDeleteTask)
            }

            Spacer(Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onVolunteerClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.VolunteerActivism, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Recruit", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDonateClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Donate", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (volunteerToDelete != null) {
        AlertDialog(
            onDismissRequest = { volunteerToDelete = null },
            title = { Text("Offboard Volunteer") },
            text = { Text("This will remove the volunteer from all project activities. Are you sure?") },
            confirmButton = {
                Button(onClick = { volunteerToDelete?.let { onDeleteVolunteer(it) }; volunteerToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Confirm Offboard")
                }
            },
            dismissButton = { TextButton(onClick = { volunteerToDelete = null }) { Text("Cancel") } }
        )
    }

    if (volunteerToEdit != null) {
        EditVolunteerDialog(volunteer = volunteerToEdit!!, onDismiss = { volunteerToEdit = null }, onConfirm = { updated -> onEditVolunteer(updated); volunteerToEdit = null })
    }
}

@Composable
fun QuickStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OverviewTab(project: Project, allTeamMembers: List<User>, volunteers: List<Volunteer>) {
    val groupLeader = volunteers.find { it.id == project.groupLeaderId }
    val assignedStaff = allTeamMembers.filter { project.teamMemberIds.contains(it.id) }
    
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        InfoRow(Icons.Default.Description, "Project Vision", project.description.ifBlank { "Vision currently being refined." })
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("LUMINARY STAFF", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            if (assignedStaff.isEmpty()) {
                Text("No staff members assigned yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                assignedStaff.forEach { staff -> StaffItem(staff) }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PROGRAM LEADERSHIP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(groupLeader?.name?.take(1)?.uppercase() ?: "L", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                    Column {
                        Text(groupLeader?.name ?: "No Leader Assigned", fontWeight = FontWeight.Bold)
                        Text("Group Leader", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        InfoRow(Icons.Default.Update, "Last Sync", project.lastUpdated)
    }
}

@Composable
fun StaffItem(user: User) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) }
        }
        Column {
            Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(user.role.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TeamTab(
    project: Project,
    allTeamMembers: List<User>,
    onUpdateTeamMembers: (String, List<String>) -> Unit
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    val assignedStaff = allTeamMembers.filter { project.teamMemberIds.contains(it.id) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Project Team (${assignedStaff.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddMemberDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Manage Team")
            }
        }

        if (assignedStaff.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No team members assigned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            assignedStaff.forEach { staff ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(staff.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(staff.name, fontWeight = FontWeight.Bold)
                            Text(staff.role.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            val newIds = project.teamMemberIds.filter { it != staff.id }
                            onUpdateTeamMembers(project.id, newIds)
                        }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredMembers = allTeamMembers.filter { 
            it.role == UserRole.TEAM &&
            (it.name.contains(searchQuery, ignoreCase = true) || it.role.name.contains(searchQuery, ignoreCase = true))
        }
        var selectedIds by remember { mutableStateOf(project.teamMemberIds.toSet()) }

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Manage Project Team", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search staff...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    
                    Column(
                        modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredMembers.forEach { member ->
                            val isSelected = selectedIds.contains(member.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedIds = if (isSelected) selectedIds - member.id else selectedIds + member.id
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold)
                                    Text(member.role.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateTeamMembers(project.id, selectedIds.toList())
                    showAddMemberDialog = false
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditVolunteerDialog(volunteer: Volunteer, onDismiss: () -> Unit, onConfirm: (Volunteer) -> Unit) {
    var name by remember { mutableStateOf(volunteer.name) }
    var email by remember { mutableStateOf(volunteer.email) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Volunteer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(volunteer.copy(name = name, email = email)) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { onDismiss() }) { Text("Cancel") } }
    )
}

@Composable
fun VolunteersTab(volunteers: List<Volunteer>, applications: List<Volunteer>, onApprove: (String) -> Unit, onReject: (String) -> Unit, onDelete: (String) -> Unit, onEdit: (Volunteer) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Active Volunteers (${volunteers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (volunteers.isEmpty()) {
            Text("No active volunteers yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        } else {
            volunteers.forEach { volunteer -> SwipeableVolunteerItem(volunteer, { onDelete(volunteer.id) }, { onEdit(volunteer) }) }
        }
        Text("Pending Applications (${applications.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (applications.isEmpty()) {
            Text("No pending applications.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        } else {
            applications.forEach { application -> ApplicationItem(application, onApprove, onReject) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableVolunteerItem(volunteer: Volunteer, onDelete: () -> Unit, onEdit: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                SwipeToDismissBoxValue.StartToEnd -> { onEdit(); false }
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 24.dp), contentAlignment = alignment) {
                Icon(if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Edit else Icons.Default.Delete, null)
            }
        },
        content = { VolunteerItem(volunteer) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun VolunteerItem(volunteer: Volunteer) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(volunteer.name.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(volunteer.name, fontWeight = FontWeight.Bold)
                Text(volunteer.skills.joinToString(", "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
        }
    }
}

@Composable
fun ApplicationItem(volunteer: Volunteer, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(volunteer.name.take(1).uppercase(), color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold) }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(volunteer.name, fontWeight = FontWeight.Bold)
                    Text(volunteer.email, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Skills: ${volunteer.skills.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onReject(volunteer.id) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Reject") }
                Button(onClick = { onApprove(volunteer.id) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) { Text("Approve") }
            }
        }
    }
}

@Composable
fun FinanceTab(project: Project, onExpensesClick: () -> Unit = {}) {
    val spentPercent = if (project.budget > 0) project.spent.toFloat() / project.budget.toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        FinanceMetric("Total Budget", "KES ${project.budget}", Color(0xFF6366F1))
        Card(onClick = onExpensesClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Total Spent", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold); Text("KES ${project.spent}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error) }
                    Icon(Icons.Default.ChevronRight, null)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { spentPercent }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape), color = MaterialTheme.colorScheme.error, trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun ActivityTab(
    project: Project, 
    volunteers: List<Volunteer>, 
    teamMembers: List<User>, 
    isSaving: Boolean,
    onToggleTask: (String, Boolean) -> Unit, 
    onAssignLeader: (String) -> Unit, 
    onAddTask: (String, String, List<String>, List<String>, com.example.luminarysolutions.data.models.AssigneeType, Long) -> Unit,
    onUpdateTask: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<com.example.luminarysolutions.data.models.Task?>(null) }
    var showLeaderSelection by remember { mutableStateOf(false) }
    val leader = volunteers.find { it.id == project.groupLeaderId }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUser = teamMembers.find { it.id == currentUserId }
    val isCEO = currentUser?.role == UserRole.CEO

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Leadership", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().clickable { showLeaderSelection = true }) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSecondary) } }
                Column(modifier = Modifier.weight(1f)) { Text(leader?.name ?: "No Leader Assigned", fontWeight = FontWeight.Bold); Text(if (leader != null) "Group Leader" else "Tap to assign a leader", style = MaterialTheme.typography.labelMedium) }
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Project Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(
                onClick = { showAddTaskDialog = true },
                enabled = !isSaving
            ) { 
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("Add Task") 
            }
        }
        if (project.tasks.isEmpty()) { Text("No tasks assigned.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) }
        else { 
            project.tasks.forEach { task -> 
                TaskItem(
                    task = task, 
                    currentUserId = currentUserId,
                    isCEO = isCEO,
                    onToggle = { onToggleTask(task.id, it) },
                    onEdit = { taskToEdit = it },
                    onDelete = { onDeleteTask(task.id) }
                ) 
            } 
        }
    }
    if (showLeaderSelection) {
        AlertDialog(
            onDismissRequest = { showLeaderSelection = false },
            title = { Text("Assign Group Leader") },
            text = { Column { volunteers.forEach { v -> Row(modifier = Modifier.fillMaxWidth().clickable { onAssignLeader(v.id); showLeaderSelection = false }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = project.groupLeaderId == v.id, onClick = null); Text(v.name) } } } },
            confirmButton = { TextButton(onClick = { showLeaderSelection = false }) { Text("Cancel") } }
        )
    }
    if (showAddTaskDialog) { 
        AddTaskDialog(
            teamMembers = teamMembers, 
            volunteers = volunteers, 
            isSaving = isSaving,
            onDismiss = { showAddTaskDialog = false }, 
            onSave = onAddTask
        ) 
    }

    if (taskToEdit != null) {
        AddTaskDialog(
            task = taskToEdit,
            teamMembers = teamMembers,
            volunteers = volunteers,
            isSaving = isSaving,
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, ids, names, type, dl ->
                onUpdateTask(taskToEdit!!.copy(
                    title = title,
                    description = desc,
                    assignedToIds = ids,
                    assignedToNames = names,
                    assigneeType = type,
                    deadline = dl
                ))
            }
        )
    }
}

@Composable
fun AddTaskDialog(
    task: com.example.luminarysolutions.data.models.Task? = null,
    teamMembers: List<User>, 
    volunteers: List<Volunteer>, 
    isSaving: Boolean = false,
    onDismiss: () -> Unit, 
    onSave: (String, String, List<String>, List<String>, com.example.luminarysolutions.data.models.AssigneeType, Long) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var desc by remember { mutableStateOf(task?.description ?: "") }
    var selectedAssigneeIds by remember { mutableStateOf(task?.assignedToIds?.toSet() ?: emptySet<String>()) }
    var type by remember { mutableStateOf(task?.assigneeType ?: com.example.luminarysolutions.data.models.AssigneeType.TEAM) }
    var deadline by remember { mutableLongStateOf(task?.deadline ?: (System.currentTimeMillis() + 604800000)) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline)

    AlertDialog(
        onDismissRequest = if (isSaving) ({}) else onDismiss,
        title = { 
            Column {
                Text(if (task == null) "Launch New Task" else "Update Milestone", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("Assign resources and set impact delivery goals.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Milestone Title") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("e.g. Community Health Survey") },
                    enabled = !isSaving
                )
                
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Task Objectives") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    minLines = 3,
                    placeholder = { Text("Detail the goals and expected impact...") },
                    enabled = !isSaving
                )
                
                Text("RESOURCE POOL", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = type == com.example.luminarysolutions.data.models.AssigneeType.TEAM,
                        onClick = { type = com.example.luminarysolutions.data.models.AssigneeType.TEAM; selectedAssigneeIds = emptySet() },
                        label = { Text("Luminary Team") },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    FilterChip(
                        selected = type == com.example.luminarysolutions.data.models.AssigneeType.VOLUNTEER,
                        onClick = { type = com.example.luminarysolutions.data.models.AssigneeType.VOLUNTEER; selectedAssigneeIds = emptySet() },
                        label = { Text("Volunteers") },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                }
                
                Box {
                    OutlinedTextField(
                        value = when {
                            selectedAssigneeIds.isEmpty() -> "Select Assignees"
                            else -> "${selectedAssigneeIds.size} Members Assigned"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign To") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = { IconButton(onClick = { expanded = true }, enabled = !isSaving) { Icon(Icons.Default.GroupAdd, null) } },
                        enabled = !isSaving
                    )
                    
                    DropdownMenu(
                        expanded = expanded, 
                        onDismissRequest = { expanded = false }, 
                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 300.dp)
                    ) {
                        val list = if (type == com.example.luminarysolutions.data.models.AssigneeType.TEAM) {
                            teamMembers.filter { it.role == UserRole.TEAM }.map { it.id to it.name }
                        } else {
                            volunteers.map { it.id to it.name }
                        }
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedAssigneeIds.size == list.size && list.isNotEmpty(), onCheckedChange = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Assign Entire Resource Pool", fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                selectedAssigneeIds = if (selectedAssigneeIds.size == list.size) emptySet() else list.map { it.first }.toSet()
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        list.forEach { (id, name) -> 
                            val isSelected = selectedAssigneeIds.contains(id)
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(Modifier.width(12.dp))
                                        Text(name)
                                    }
                                }, 
                                onClick = { 
                                    selectedAssigneeIds = if (isSelected) selectedAssigneeIds - id else selectedAssigneeIds + id
                                }
                            ) 
                        }
                    }
                }
                
                Text("IMPACT TIMELINE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(deadline)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Impact Deadline") },
                    modifier = Modifier.fillMaxWidth().clickable { if (!isSaving) showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = { 
            Button(
                onClick = { 
                    val names = if (type == com.example.luminarysolutions.data.models.AssigneeType.TEAM) {
                        teamMembers.filter { selectedAssigneeIds.contains(it.id) }.map { it.name }
                    } else {
                        volunteers.filter { selectedAssigneeIds.contains(it.id) }.map { it.name }
                    }
                    onSave(title, desc, selectedAssigneeIds.toList(), names, type, datePickerState.selectedDateMillis ?: deadline)
                }, 
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotBlank() && selectedAssigneeIds.isNotEmpty() && !isSaving
            ) { 
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text(if (task == null) "Deploy Task" else "Update Milestone", fontWeight = FontWeight.Bold)
                }
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Discard") 
            } 
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { Button(onClick = { showDatePicker = false }) { Text("Set Deadline") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TaskItem(
    task: com.example.luminarysolutions.data.models.Task, 
    currentUserId: String,
    isCEO: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDelete: () -> Unit
) {
    val isAssigner = task.assignedById == currentUserId
    val isOverdue = task.deadline < System.currentTimeMillis() && !task.isDone
    val canToggle = !isCEO && !isAssigner

    Surface(
        shape = RoundedCornerShape(24.dp), 
        color = Color.White, 
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Priority/Status Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (task.isDone) Color(0xFF10B981) else Color(0xFFF1F5F9))
                    .clickable(enabled = canToggle) { onToggle(!task.isDone) },
                contentAlignment = Alignment.Center
            ) {
                if (task.isDone) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title, 
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold, 
                        textDecoration = if (task.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Deadline Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOverdue) Color(0xFFFEE2E2) else Color(0xFFECFDF5)
                    ) {
                        Text(
                            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.deadline)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) Color(0xFFEF4444) else Color(0xFF059669),
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        if (task.assignedToNames.isEmpty()) "Resource Hub" else task.assignedToNames.joinToString(", "), 
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            val canManage = isCEO || isAssigner
            if (canManage) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.LightGray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Task") },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                            onClick = { showMenu = false; onEdit(task) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444)) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column { Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold); Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun FinanceMetric(label: String, value: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, fontWeight = FontWeight.Medium); Text(value, fontWeight = FontWeight.Black, color = color, fontSize = 18.sp) }
    }
}

@Composable
fun ModernDetailCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp), modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline) }
            Spacer(Modifier.height(16.dp)); content()
        }
    }
}

fun getProjectStatusColorLocal(status: String): Color = when (status) {
    "Completed" -> Color(0xFF10B981)
    "At Risk" -> Color(0xFFF43F5E)
    "Ongoing" -> Color(0xFF3B82F6)
    else -> Color(0xFF6B7280)
}

@Composable
fun ProjectStatusBadgeLocal(status: String) {
    val color = getProjectStatusColorLocal(status)
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Text(text = status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
    }
}
