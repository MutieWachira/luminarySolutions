package com.example.luminarysolutions.ui.ceo.Dashboard.Lumisphere

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.firebase.LumiSphereOverviewDashboardStats
import com.example.luminarysolutions.data.models.Event
import com.example.luminarysolutions.data.models.Project
import com.example.luminarysolutions.data.models.Volunteer
import com.example.luminarysolutions.ui.ceo.CEODashboardUiState
import com.example.luminarysolutions.ui.ceo.CEODashboardViewModel
import com.example.luminarysolutions.ui.ceo.Dashboard.Luminary.BreakdownItem
import com.example.luminarysolutions.ui.ceo.Dashboard.Luminary.YearSelector
import com.example.luminarysolutions.ui.common.ExecutiveNavigationBar
import com.example.luminarysolutions.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LumiSphere Details Screen - NGO Impact & Sustainability Overview.
 * Optimized for production with automatic volunteer project assignment and detailed analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiSphereDetailsScreen(
    navController: NavController,
    dashboardViewModel: CEODashboardViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val selectedYear by dashboardViewModel.selectedYear.collectAsState()

    LumiSphereDetailsContent(
        uiState = uiState,
        selectedYear = selectedYear,
        onYearSelected = { dashboardViewModel.updateSelectedYear(it) },
        onBackClick = { navController.popBackStack() },
        onProjectClick = { projectId ->
            navController.navigate(Screen.ProjectDetails.createRoute(projectId))
        },
        onVolunteerClick = { volunteerId ->
            navController.navigate(Screen.VolunteerDetails.createRoute(volunteerId))
        },
        onNavigateHome = { navController.navigate(Screen.CEODashboard.route) },
        onNavigateProjects = { navController.navigate(Screen.Projects.route) },
        onNavigateReports = { navController.navigate(Screen.Reports.route) },
        viewModel = dashboardViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiSphereDetailsContent(
    uiState: CEODashboardUiState,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onVolunteerClick: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateReports: () -> Unit,
    viewModel: CEODashboardViewModel
) {
    val selectedTabIndex = uiState.lumiSphereSelectedTabIndex
    var showAddProgramDialog by remember { mutableStateOf(false) }
    var programToEdit by remember { mutableStateOf<Project?>(null) }
    var programToDelete by remember { mutableStateOf<Project?>(null) }
    var volunteerToEdit by remember { mutableStateOf<Volunteer?>(null) }
    var volunteerToDelete by remember { mutableStateOf<Volunteer?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Safety: Confirmation for removing a volunteer
    if (volunteerToDelete != null) {
        AlertDialog(
            onDismissRequest = { volunteerToDelete = null },
            title = { Text("Remove Volunteer", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${volunteerToDelete?.name}? This will also revoke their access to the volunteer portal.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        volunteerToDelete?.let { v -> viewModel.deleteVolunteer(v.id) { } }
                        volunteerToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { volunteerToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Interactive Edit Dialog for Volunteer Data
    if (volunteerToEdit != null) {
        EditVolunteerDialog(
            volunteer = volunteerToEdit!!,
            isSaving = uiState.isSaving,
            onDismiss = { volunteerToEdit = null },
            onConfirm = { updatedVolunteer ->
                viewModel.updateVolunteer(updatedVolunteer) { success ->
                    if (success) volunteerToEdit = null
                }
            }
        )
    }

    // Initiative Deletion Workflow
    if (programToDelete != null) {
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
            text = { Text("Deleting '${programToDelete?.name}' will permanently remove all associated data. This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        programToDelete?.let { project -> viewModel.deleteProgram(project.id) { } }
                        programToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Program Creation Dialog
    if (showAddProgramDialog) {
        AddProgramDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showAddProgramDialog = false },
            onConfirm = { newProject, uri ->
                viewModel.addProgram(newProject, uri) { success ->
                    if (success) showAddProgramDialog = false
                }
            }
        )
    }

    // Initiative Update Dialog
    if (programToEdit != null) {
        EditProgramDialog(
            program = programToEdit!!,
            isSaving = uiState.isSaving,
            onDismiss = { programToEdit = null },
            onConfirm = { updatedProject, uri ->
                viewModel.updateProgram(updatedProject, uri) { success ->
                    if (success) programToEdit = null
                }
            }
        )
    }

    // Event Deletion Workflow
    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Event", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${eventToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventToDelete?.let { event -> viewModel.deleteEvent(event.id) { } }
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Event Creation/Edit Dialogs
    if (showAddEventDialog) {
        AddEventDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { newEvent ->
                viewModel.addEvent(newEvent) { success ->
                    if (success) showAddEventDialog = false
                }
            }
        )
    }

    if (eventToEdit != null) {
        EditEventDialog(
            event = eventToEdit!!,
            isSaving = uiState.isSaving,
            onDismiss = { eventToEdit = null },
            onConfirm = { updatedEvent ->
                viewModel.updateEvent(updatedEvent) { success ->
                    if (success) eventToEdit = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LumiSphere", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("IMPACT MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ExecutiveNavigationBar(
                currentScreen = "home",
                onNavigateToHome = onNavigateHome,
                onNavigateToProjects = onNavigateProjects,
                onNavigateToReports = onNavigateReports,
                onAddClick = { showAddProgramDialog = true }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { showAddProgramDialog = true },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, "Add Program") }
            } else if (selectedTabIndex == 2) {
                FloatingActionButton(
                    onClick = { showAddEventDialog = true },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Event, "Add Event") }
            }
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
            LumiSphereHeaderCard(uiState.lumiSphereStats.impactScore)

            LumiSphereTabsRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { viewModel.updateLumiSphereTab(it) }
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> ImpactOverviewTab(uiState, selectedYear, onYearSelected)
                    1 -> ProgramsTab(uiState, onProjectClick, viewModel, onEditClick = { programToEdit = it }, onDeleteClick = { programToDelete = it })
                    2 -> EventsTab(uiState, viewModel, onEditClick = { eventToEdit = it }, onDeleteClick = { eventToDelete = it })
                    3 -> DonationsTab(uiState)
                    4 -> VolunteersTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        onEditVolunteer = { volunteerToEdit = it },
                        onDeleteVolunteer = { volunteerToDelete = it },
                        onVolunteerClick = { onVolunteerClick(it.id) }
                    )
                    5 -> BeneficiariesTab(uiState)
                }
            }
        }
    }
}

@Composable
fun ImpactOverviewTab(uiState: CEODashboardUiState, selectedYear: Int, onYearSelected: (Int) -> Unit) {
    val stats = uiState.lumiSphereStats
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Strategic Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ImpactStatCard(label = "Total Funds", value = formatLumiAmount(stats.totalDonations), trend = "+14%", icon = Icons.Default.VolunteerActivism, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            ImpactStatCard(label = "Impact Reach", value = formatBeneficiaries(stats.totalBeneficiaries), trend = "+8%", icon = Icons.Default.Groups, color = Color(0xFF0EA5E9), modifier = Modifier.weight(1f))
        }
        ImpactGrowthSection(stats, selectedYear, onYearSelected)
        SustainabilityInsightsCard()
    }
}

@Composable
fun ImpactGrowthSection(stats: LumiSphereOverviewDashboardStats, selectedYear: Int, onYearSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Growth Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            YearSelector(selectedYear = selectedYear, onYearSelected = onYearSelected)
        }
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFF8F9FA), modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text("Real-time impact data for $selectedYear", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProgramsTab(uiState: CEODashboardUiState, onProjectClick: (String) -> Unit, viewModel: CEODashboardViewModel, onEditClick: (Project) -> Unit, onDeleteClick: (Project) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Stats Overview
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ImpactStatCard(
                label = "Active Programs", 
                value = uiState.initiatives.size.toString(), 
                trend = "+2 this month", 
                icon = Icons.Default.Public, 
                color = Color(0xFF10B981), 
                modifier = Modifier.weight(1f)
            )
            ImpactStatCard(
                label = "Global Reach", 
                value = uiState.initiatives.map { it.location }.distinct().size.toString(), 
                trend = "Locations", 
                icon = Icons.Default.LocationOn, 
                color = Color(0xFF0EA5E9), 
                modifier = Modifier.weight(1f)
            )
        }

        SearchTextField(value = uiState.programSearchQuery, onValueChange = { viewModel.updateProgramSearchQuery(it) }, placeholder = "Search programs, locations or categories...", modifier = Modifier.fillMaxWidth())
        
        Text("Impact Initiatives", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        if (uiState.initiatives.isEmpty()) {
            EmptyStatePlaceholder("No programs match your search.")
        } else {
            uiState.initiatives.forEach { program ->
                SwipeableProgramCard(
                    program = program,
                    onClick = { onProjectClick(program.id) },
                    onEdit = { onEditClick(program) },
                    onDelete = { onDeleteClick(program) }
                )
            }
        }
    }
}

@Composable
fun EventsTab(uiState: CEODashboardUiState, viewModel: CEODashboardViewModel, onEditClick: (Event) -> Unit, onDeleteClick: (Event) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SearchTextField(value = uiState.eventSearchQuery, onValueChange = { viewModel.updateEventSearchQuery(it) }, placeholder = "Search upcoming events...", modifier = Modifier.fillMaxWidth())

        Text("Upcoming Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        if (uiState.events.isEmpty()) {
            EmptyStatePlaceholder("No upcoming events scheduled.")
        } else {
            uiState.events.forEach { event ->
                EventCard(
                    event = event,
                    onEdit = { onEditClick(event) },
                    onDelete = { onDeleteClick(event) }
                )
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateDisplay = SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault()).format(Date(event.date))
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (!event.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Text(dateDisplay, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.LightGray) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                            DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { expanded = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(event.location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Text(event.description, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF8F9FA)) {
                        Text(event.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("${event.attendees.size} attending", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun VolunteersTab(
    uiState: CEODashboardUiState,
    viewModel: CEODashboardViewModel,
    onEditVolunteer: (Volunteer) -> Unit,
    onDeleteVolunteer: (Volunteer) -> Unit,
    onVolunteerClick: (Volunteer) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ImpactStatCard(label = "Active Members", value = uiState.volunteers.size.toString(), trend = "verified", icon = Icons.Default.Groups, color = Color(0xFF6366F1), modifier = Modifier.weight(1f))
            ImpactStatCard(label = "Applications", value = uiState.volunteerApplications.size.toString(), trend = "pending", icon = Icons.Default.PendingActions, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        }

        SearchTextField(value = uiState.volunteerSearchQuery, onValueChange = { viewModel.updateVolunteerSearchQuery(it) }, placeholder = "Search volunteers...", modifier = Modifier.fillMaxWidth())

        Text("Volunteer Applications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        if (uiState.volunteerApplications.isEmpty()) {
            EmptyStatePlaceholder("No pending applications found.")
        } else {
            uiState.volunteerApplications.forEach { volunteer ->
                VolunteerApplicationCard(volunteer, onApprove = { viewModel.updateVolunteerStatus(volunteer.id, "Approved") { } }, onReject = { viewModel.updateVolunteerStatus(volunteer.id, "Rejected") { } }, onClick = { onVolunteerClick(volunteer) })
            }
        }

        Text("Active Volunteers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        if (uiState.volunteers.isEmpty()) {
            EmptyStatePlaceholder("No active volunteers in the network.")
        } else {
            uiState.volunteers.forEach { volunteer ->
                SwipeableActiveVolunteerCard(volunteer, onEdit = { onEditVolunteer(volunteer) }, onDelete = { onDeleteVolunteer(volunteer) }, onClick = { onVolunteerClick(volunteer) })
            }
        }
    }
}

@Composable
fun VolunteerApplicationCard(volunteer: Volunteer, onApprove: () -> Unit, onReject: () -> Unit, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFFF1F5F9)), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(volunteer.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(volunteer.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B).copy(alpha = 0.1f)) {
                    Text("PENDING", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) { Text("Approve") }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("Reject") }
            }
        }
    }
}

@Composable
fun ActiveVolunteerCard(volunteer: Volunteer, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFFF1F5F9)), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                if (!volunteer.profileImageUrl.isNullOrBlank()) {
                    AsyncImage(model = volunteer.profileImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color(0xFF10B981)) }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(volunteer.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(volunteer.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ProgramCard(program: Project, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(72.dp)
                ) {
                    if (!program.imageUrl.isNullOrBlank()) {
                        AsyncImage(model = program.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Public, null, tint = Color.LightGray, modifier = Modifier.size(32.dp)) }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(program.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(4.dp), color = if (program.status == "Ongoing") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f)) {
                            Text(program.status.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (program.status == "Ongoing") Color(0xFF10B981) else Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(program.location.ifBlank { "Global" }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text(program.category.ifBlank { "Impact" }, style = MaterialTheme.typography.labelSmall, color = Color(0xFF0EA5E9), fontWeight = FontWeight.Bold)
                }
            }

            Text(program.description, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Project Tasks Progress
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Task Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${(program.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { program.progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color(0xFF10B981), trackColor = Color(0xFF10B981).copy(alpha = 0.1f))
                }

                // Donations Progress
                val donationProgress = if (program.budget > 0) (program.spent.toFloat() / program.budget.toFloat()).coerceIn(0f, 1f) else 0f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Donation Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("KSh ${formatLumiAmount(program.spent)} / ${formatLumiAmount(program.budget)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { donationProgress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color(0xFF0EA5E9), trackColor = Color(0xFF0EA5E9).copy(alpha = 0.1f))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Groups, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(program.volunteers.size.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableProgramCard(program: Project, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.EndToStart -> { onDelete(); false }
            SwipeToDismissBoxValue.StartToEnd -> { onEdit(); false }
            else -> false
        }
    })
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color(0xFFF43F5E) else Color(0xFF10B981)
            val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icons.Default.Delete else Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) {
        ProgramCard(program, onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableActiveVolunteerCard(volunteer: Volunteer, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.EndToStart -> { onDelete(); false }
            SwipeToDismissBoxValue.StartToEnd -> { onEdit(); false }
            else -> false
        }
    })
    SwipeToDismissBox(state = dismissState, backgroundContent = {
        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color(0xFFF43F5E) else Color(0xFF6366F1)
        val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 20.dp), contentAlignment = alignment) {
            Icon(imageVector = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icons.Default.Delete else Icons.Default.Edit, contentDescription = null, tint = Color.White)
        }
    }) {
        ActiveVolunteerCard(volunteer, onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetailsDialog(
    volunteer: Volunteer,
    onDismiss: () -> Unit,
    onApprove: (Volunteer) -> Unit,
    onReject: (Volunteer) -> Unit,
    onEdit: (Volunteer) -> Unit
) {
    // This dialog is deprecated in favor of VolunteerDetailsScreen
    // but keeping it for now if needed elsewhere, fixed the field reference
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    var projectName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(volunteer.projectIds) {
        volunteer.projectIds.firstOrNull()?.let { id ->
            db.collection("lumisphere").document("projects").collection("items").document(id).get()
                .addOnSuccessListener { doc -> projectName = doc.getString("name") }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
                    AsyncImage(model = volunteer.profileImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                }
                Column {
                    Text(volunteer.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(volunteer.status.uppercase(), style = MaterialTheme.typography.labelMedium, color = if(volunteer.status == "Approved") Color(0xFF10B981) else Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
            DetailRow(Icons.Default.Email, "Official Email", volunteer.email)
            DetailRow(Icons.Default.Phone, "Phone Number", volunteer.phoneNumber.ifBlank { "Unspecified" })
            DetailRow(Icons.Default.Handyman, "Skill Set", volunteer.skills.joinToString(", ").ifBlank { "General Support" })
            if (volunteer.projectIds.isNotEmpty()) DetailRow(Icons.Default.Public, "Assigned Initiative", projectName ?: "Loading Program...")
            
            Text("Personal Motivation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8F9FA)) {
                Text(volunteer.motivation.ifBlank { "No motivation statement provided." }, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (volunteer.status == "Pending") {
                    Button(onClick = { onApprove(volunteer) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) { Text("Approve Access", fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedButton(onClick = { onEdit(volunteer) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Edit Profile") }
                }
                OutlinedButton(onClick = { onReject(volunteer) }, modifier = Modifier.height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("Revoke") }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DonationsTab(uiState: CEODashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Funding Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFF8F9FA), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color(0xFFE2E8F0), strokeWidth = 10.dp)
                    CircularProgressIndicator(progress = { 0.72f }, modifier = Modifier.fillMaxSize(), color = Color(0xFF10B981), strokeWidth = 10.dp)
                    Text("72%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BreakdownItem("Individuals", "KSh 4.2M", "72%", Color(0xFF10B981))
                    BreakdownItem("Corporates", "KSh 1.8M", "28%", Color(0xFF0EA5E9))
                }
            }
        }
    }
}

@Composable
fun BeneficiariesTab(uiState: CEODashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Impact Demographics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFF8F9FA), modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DemographicRow("Women & Children", 0.65f, Color(0xFF10B981))
                DemographicRow("Rural Communities", 0.35f, Color(0xFF0EA5E9))
            }
        }
    }
}

@Composable
fun DemographicRow(label: String, percentage: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text("${(percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        LinearProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = color, trackColor = color.copy(alpha = 0.1f))
    }
}

@Composable
fun LumiSphereHeaderCard(score: Float) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), shape = RoundedCornerShape(24.dp), color = Color(0xFF111418)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(60.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Public, null, tint = Color.White, modifier = Modifier.size(28.dp)) } }
            Column(modifier = Modifier.weight(1f)) {
                Text("LumiSphere", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                Text("Sustainable Global Impact", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { score / 10f }, modifier = Modifier.size(54.dp), color = Color(0xFF10B981), strokeWidth = 5.dp)
                Text(score.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun LumiSphereTabsRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("OVERVIEW", "PROGRAMS", "EVENTS", "DONATIONS", "VOLUNTEERS", "IMPACT")
    SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent, contentColor = Color(0xFF10B981), edgePadding = 20.dp, divider = {}) {
        tabs.forEachIndexed { index, title -> Tab(selected = index == selectedTabIndex, onClick = { onTabSelected(index) }, text = { Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (index == selectedTabIndex) Color(0xFF10B981) else Color.Gray) }) }
    }
}

@Composable
fun ImpactStatCard(label: String, value: String, trend: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF8F9FA), modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) } }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Text(trend, style = MaterialTheme.typography.labelSmall, color = if(trend.startsWith("+")) Color(0xFF10B981) else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SustainabilityInsightsCard() {
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF10B981).copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF10B981))
            Text("Solar adoption in rural programs has reduced operational costs by 32% this year.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgramDialog(isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (Project, Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sustainability") }
    var budget by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val categories = listOf("Sustainability", "Education", "Healthcare", "Environment", "Community", "Emergency", "Entertainment")

    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("New Impact Initiative", fontWeight = FontWeight.Black) }, 
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Surface(
                    onClick = { launcher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                                Text("Add Program Image", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Program Name") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) 
                
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) }, 
                                onClick = { 
                                    category = statusOption
                                    categoryExpanded = false 
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Target Location") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) 
                
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (KSh)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) 

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Impact Mission") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 3) 
            }
        }, 
        confirmButton = { 
            Button(
                enabled = !isSaving && name.isNotBlank(),
                onClick = { 
                    onConfirm(Project(
                        name = name, 
                        description = description, 
                        location = location, 
                        category = category,
                        budget = budget.toIntOrNull() ?: 0,
                        status = "Planned",
                        startDate = System.currentTimeMillis()
                    ), imageUri) 
                }, 
                shape = RoundedCornerShape(12.dp)
            ) { 
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Create Program") 
            }
        }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProgramDialog(program: Project, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (Project, Uri?) -> Unit) {
    var name by remember { mutableStateOf(program.name) }
    var description by remember { mutableStateOf(program.description) }
    var location by remember { mutableStateOf(program.location) }
    var category by remember { mutableStateOf(program.category) }
    var budget by remember { mutableStateOf(program.budget.toString()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val categories = listOf("Sustainability", "Education", "Healthcare", "Environment", "Community", "Emergency")

    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("Edit Initiative", fontWeight = FontWeight.Black) }, 
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Surface(
                    onClick = { launcher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (!program.imageUrl.isNullOrEmpty()) {
                        AsyncImage(model = program.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                                Text("Change Program Image", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Program Name") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) 
                
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) }, 
                                onClick = { 
                                    category = option
                                    categoryExpanded = false 
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) 
                
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (KSh)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) 

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 3) 
            }
        }, 
        confirmButton = { 
            Button(
                enabled = !isSaving && name.isNotBlank(),
                onClick = { 
                    onConfirm(program.copy(
                        name = name, 
                        description = description, 
                        location = location, 
                        category = category,
                        budget = budget.toIntOrNull() ?: 0
                    ), imageUri) 
                }, 
                shape = RoundedCornerShape(12.dp)
            ) { 
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Save Changes") 
            }
        }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddEventDialog(isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (Event) -> Unit) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule New Event", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Event Type") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(Event(title = title, location = location, description = description, type = type)) }, shape = RoundedCornerShape(12.dp)) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditEventDialog(event: Event, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (Event) -> Unit) {
    var title by remember { mutableStateOf(event.title) }
    var location by remember { mutableStateOf(event.location) }
    var description by remember { mutableStateOf(event.description) }
    var type by remember { mutableStateOf(event.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Event Details", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Event Type") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(event.copy(title = title, location = location, description = description, type = type)) }, shape = RoundedCornerShape(12.dp)) { Text("Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditVolunteerDialog(volunteer: Volunteer, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (Volunteer) -> Unit) {
    var name by remember { mutableStateOf(volunteer.name) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit Volunteer", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { onConfirm(volunteer.copy(name = name)) }, shape = RoundedCornerShape(12.dp)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

fun formatBeneficiaries(count: Int): String = if (count > 1000) "${count/1000}K+" else count.toString()
fun formatLumiAmount(amount: Int): String = "KSh ${amount/1000}K"

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
