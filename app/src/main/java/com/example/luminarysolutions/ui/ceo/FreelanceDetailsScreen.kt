package com.example.luminarysolutions.ui.ceo

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.Team
import com.example.luminarysolutions.data.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelanceDetailsScreen(
    navController: NavController,
    projectId: String,
    viewModel: FreelanceDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More actions */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Unknown error")
                }
            } else {
                uiState.freelance?.let { freelance ->
                    FreelanceDetailsContent(
                        freelance = freelance,
                        assignedTeam = uiState.assignedTeam,
                        applicants = uiState.applicants,
                        onAssign = { viewModel.assignToTeam(it) },
                        onRemove = { viewModel.removeFromTeam(it) },
                        onReject = { viewModel.rejectApplicant(it) },
                        onAddTask = { title, desc, ids, names, dl ->
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            viewModel.addTask(title, desc, ids, names, dl, currentUserId)
                        },
                        onUpdateTask = { viewModel.updateTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleTask = { id, done ->
                            viewModel.toggleTaskStatus(id, done)
                        },
                        allTeamMembers = uiState.allTeamMembers
                    )
                }
            }
        }
    }
}

@Composable
fun FreelanceDetailsContent(
    freelance: Freelance,
    assignedTeam: List<Team>,
    applicants: List<User>,
    allTeamMembers: List<Team>,
    onAssign: (String) -> Unit,
    onRemove: (String) -> Unit,
    onReject: (String) -> Unit,
    onAddTask: (String, String, List<String>, List<String>, Long) -> Unit,
    onUpdateTask: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Tasks", "Team", "Applicants (${applicants.size})")
    val scrollState = rememberScrollState()
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (showAddMemberDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredMembers = allTeamMembers.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || it.department.contains(searchQuery, ignoreCase = true)
        }
        
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Manage Service Team", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search team...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    
                    Column(
                        modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredMembers.forEach { member ->
                            val isAssigned = assignedTeam.any { it.id == member.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isAssigned) onRemove(member.id) else onAssign(member.id)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(checked = isAssigned, onCheckedChange = null)
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold)
                                    Text(member.jobtitle, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAddMemberDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Column {
                if (!freelance.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = freelance.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when(freelance.status) {
                                "Active" -> Color(0xFF0EA5E9).copy(alpha = 0.1f)
                                "Completed" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "Pending" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                freelance.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = when(freelance.status) {
                                    "Active" -> Color(0xFF0EA5E9)
                                    "Completed" -> Color(0xFF10B981)
                                    "Pending" -> Color(0xFFF59E0B)
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(freelance.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        freelance.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        freelance.category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Tab Row
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when (selectedTab) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Progress Card
                    FreelanceDetailCard(
                        title = "Service Completion",
                        icon = Icons.Default.Timeline
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${(freelance.progress * 100).toInt()}% Complete",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { freelance.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        }
                    }
                    FreelanceInfoRow(Icons.Default.Description, "Description", freelance.description.ifBlank { "No description provided." })
                    FreelanceInfoRow(Icons.Default.Inventory2, "Category", freelance.category)
                    FreelanceInfoRow(Icons.Default.Groups, "Team Size", "${assignedTeam.size} members assigned")
                    FreelanceInfoRow(Icons.Default.Person, "Applicants", "${applicants.size} clients applied")
                }
            }
            1 -> {
                FreelanceTasksTab(
                    freelance = freelance,
                    assignedTeam = assignedTeam,
                    onAddTask = onAddTask,
                    onUpdateTask = onUpdateTask,
                    onDeleteTask = onDeleteTask,
                    onToggleTask = onToggleTask
                )
            }
            2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Service Team (${assignedTeam.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = { showAddMemberDialog = true }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Manage")
                        }
                    }
                    
                    if (assignedTeam.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No team members assigned yet.", color = Color.Gray)
                        }
                    } else {
                        assignedTeam.forEach { member ->
                            MemberCard(member = member, actionIcon = Icons.Default.RemoveCircleOutline, onAction = { onRemove(member.id) }, actionColor = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            3 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (applicants.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No applicants for this service.", color = Color.Gray)
                        }
                    } else {
                        applicants.forEach { user ->
                            ApplicantCard(
                                user = user,
                                onApprove = { onAssign(user.id) },
                                onReject = { onReject(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: Team, actionIcon: ImageVector, onAction: () -> Unit, actionColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                if (!member.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = member.imageUrl,
                        contentDescription = member.name,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold)
                Text("${member.jobtitle} • ${member.department}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            IconButton(onClick = onAction) {
                Icon(actionIcon, null, tint = actionColor)
            }
        }
    }
}

@Composable
fun ApplicantCard(user: User, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = Color(0xFF6366F1).copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF6366F1))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FreelanceInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun FreelanceDetailCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                Text(title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun FreelanceTasksTab(
    freelance: Freelance,
    assignedTeam: List<Team>,
    onAddTask: (String, String, List<String>, List<String>, Long) -> Unit,
    onUpdateTask: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<com.example.luminarysolutions.data.models.Task?>(null) }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Service Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Task")
            }
        }

        if (freelance.tasks.isEmpty()) {
            Text("No tasks assigned to this service.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            freelance.tasks.forEach { task ->
                FreelanceTaskItem(
                    task = task, 
                    currentUserId = currentUserId,
                    onToggle = { onToggleTask(task.id, it) },
                    onEdit = { taskToEdit = it },
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }
    }

    if (showAddTaskDialog) {
        TaskDialog(
            assignedTeam = assignedTeam,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, ids, names, dl ->
                onAddTask(title, desc, ids, names, dl)
                showAddTaskDialog = false
            }
        )
    }

    if (taskToEdit != null) {
        TaskDialog(
            task = taskToEdit,
            assignedTeam = assignedTeam,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, desc, ids, names, dl ->
                onUpdateTask(taskToEdit!!.copy(
                    title = title,
                    description = desc,
                    assignedToIds = ids,
                    assignedToNames = names,
                    deadline = dl
                ))
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskDialog(
    task: com.example.luminarysolutions.data.models.Task? = null,
    assignedTeam: List<Team>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>, List<String>, Long) -> Unit
) {
    var taskTitle by remember { mutableStateOf(task?.title ?: "") }
    var taskDesc by remember { mutableStateOf(task?.description ?: "") }
    var selectedAssigneeIds by remember { mutableStateOf(task?.assignedToIds?.toSet() ?: emptySet<String>()) }
    var deadline by remember { mutableLongStateOf(task?.deadline ?: (System.currentTimeMillis() + (86400000 * 7))) }
    var expandedAssignee by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline)

    val selectedNames = assignedTeam.filter { selectedAssigneeIds.contains(it.id) }.map { it.name }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text(if (task == null) "New Task" else "Edit Task", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("Define project milestones and assign team.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle, 
                    onValueChange = { taskTitle = it }, 
                    label = { Text("Task Title") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("e.g. Design System Audit") }
                )
                
                OutlinedTextField(
                    value = taskDesc, 
                    onValueChange = { taskDesc = it }, 
                    label = { Text("Task Description") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    minLines = 3,
                    placeholder = { Text("Provide details about the requirements...") }
                )
                
                Text("ASSIGNMENT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Box {
                    OutlinedTextField(
                        value = when {
                            selectedAssigneeIds.isEmpty() -> "Select Team Members"
                            selectedAssigneeIds.size == assignedTeam.size && assignedTeam.isNotEmpty() -> "Entire Team"
                            else -> "${selectedAssigneeIds.size} Members Selected"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign To") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = { IconButton(onClick = { expandedAssignee = true }) { Icon(Icons.Default.PersonAdd, null) } }
                    )
                    
                    DropdownMenu(
                        expanded = expandedAssignee, 
                        onDismissRequest = { expandedAssignee = false },
                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 300.dp)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedAssigneeIds.size == assignedTeam.size && assignedTeam.isNotEmpty(), onCheckedChange = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Assign All Members", fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                selectedAssigneeIds = if (selectedAssigneeIds.size == assignedTeam.size) emptySet() else assignedTeam.map { it.id }.toSet()
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        assignedTeam.forEach { member ->
                            val isSelected = selectedAssigneeIds.contains(member.id)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(member.name, fontWeight = FontWeight.Medium)
                                            Text(member.jobtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedAssigneeIds = if (isSelected) selectedAssigneeIds - member.id else selectedAssigneeIds + member.id
                                }
                            )
                        }
                    }
                }
                
                Text("SCHEDULE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(deadline)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Deadline Date") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
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
                    if (taskTitle.isNotBlank()) {
                        onConfirm(taskTitle, taskDesc, selectedAssigneeIds.toList(), selectedNames, datePickerState.selectedDateMillis ?: deadline)
                    }
                }, 
                shape = RoundedCornerShape(16.dp),
                enabled = taskTitle.isNotBlank()
            ) {
                Text(if (task == null) "Create Task" else "Update Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
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
fun FreelanceTaskItem(
    task: com.example.luminarysolutions.data.models.Task, 
    currentUserId: String,
    onToggle: (Boolean) -> Unit,
    onEdit: (com.example.luminarysolutions.data.models.Task) -> Unit,
    onDelete: () -> Unit
) {
    val isAssigner = task.assignedById == currentUserId
    val isOverdue = task.deadline < System.currentTimeMillis() && !task.isDone
    
    // In FreelanceDetailsScreen, the role is not explicitly passed yet. 
    // However, usually only CEO/Assigner should manage.
    // For now, let's allow only the assigner or if we can determine CEO status.
    // Assuming for now that the person viewing this screen in CEO module has high privileges.
    val canManage = isAssigner // Will refine if needed

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Status Indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isDone) Color(0xFF10B981) else Color(0xFFF1F5F9))
                    .clickable { onToggle(!task.isDone) },
                contentAlignment = Alignment.Center
            ) {
                if (task.isDone) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    color = if (task.isDone) Color.Gray else Color.Black
                )
                
                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Deadline Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isOverdue) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer, 
                                null, 
                                tint = if (isOverdue) Color(0xFFEF4444) else Color.Gray, 
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(task.deadline)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) Color(0xFFEF4444) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Assignees
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Group, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(
                            if (task.assignedToNames.isEmpty()) "Everyone" else if (task.assignedToNames.size > 2) "${task.assignedToNames.size} members" else task.assignedToNames.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            if (canManage) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.LightGray)
                    }
                    DropdownMenu(
                        expanded = showMenu, 
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
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
