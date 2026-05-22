package com.example.luminarysolutions.ui.ceo

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelanceDetailsScreen(
    navController: NavController,
    projectId: String,
    viewModel: FreelanceDetailsViewModel = viewModel()
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
                        onReject = { viewModel.rejectApplicant(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FreelanceDetailsContent(
    freelance: Freelance,
    assignedTeam: List<User>,
    applicants: List<User>,
    onAssign: (String) -> Unit,
    onRemove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Assigned Team", "Applicants (${applicants.size})")
    val scrollState = rememberScrollState()

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
                    InfoRow(Icons.Default.Description, "Description", freelance.description.ifBlank { "No description provided." })
                    InfoRow(Icons.Default.Inventory2, "Category", freelance.category)
                    InfoRow(Icons.Default.Groups, "Team Size", "${assignedTeam.size} members assigned")
                    InfoRow(Icons.Default.Person, "Applicants", "${applicants.size} clients applied")
                }
            }
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (assignedTeam.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No team members assigned yet.", color = Color.Gray)
                        }
                    } else {
                        assignedTeam.forEach { user ->
                            MemberCard(user = user, actionIcon = Icons.Default.RemoveCircleOutline, onAction = { onRemove(user.id) }, actionColor = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            2 -> {
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
fun MemberCard(user: User, actionIcon: ImageVector, onAction: () -> Unit, actionColor: Color) {
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
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.role.name, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
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
