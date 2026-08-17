package com.example.luminarysolutions.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.DashboardStats
import com.example.luminarysolutions.data.models.Freelance
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.navigation.Screen

/**
 * ClientDashboardScreen: A production-ready, professional dashboard for Luminary clients.
 * Implements MVVM pattern and follows modern Material 3 design guidelines.
 */
@Composable
fun ClientDashboardScreen(
    navController: NavController,
    viewModel: ClientDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { ClientBottomNav(navController, currentRoute = Screen.ClientDashboard.route) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        when (val state = uiState) {
            is ClientDashboardUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ClientDashboardUiState.Success -> {
                SuccessContent(
                    state = state,
                    padding = padding,
                    navController = navController
                )
            }
            is ClientDashboardUiState.Error -> {
                ErrorContent(message = state.message)
            }
        }
    }
}

@Composable
private fun SuccessContent(
    state: ClientDashboardUiState.Success,
    padding: PaddingValues,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Personalized Header
        item {
            ClientProfileHeader(user = state.user)
        }

        // 2. Dashboard Stats
        item {
            DashboardStatsSection(stats = state.stats)
        }

        // 3. Quick Actions
        item {
            QuickActionsSection(navController = navController)
        }

        // 4. Ongoing Projects
        if (state.ongoingProjects.isNotEmpty()) {
            item {
                SectionHeader(title = "Ongoing Projects", actionText = "View All") {
                    // Navigate to Projects list
                }
                Spacer(Modifier.height(12.dp))
                OngoingProjectsCarousel(
                    projects = state.ongoingProjects,
                    onProjectClick = { id -> 
                        navController.navigate(Screen.ClientServiceDetails.createRoute(id))
                    }
                )
            }
        }

        // 5. Featured Services
        item {
            SectionHeader(title = "New Opportunities", actionText = "Explore") {
                navController.navigate(Screen.ClientFreelanceServices.route)
            }
        }
        
        items(state.featuredServices) { service ->
            ServiceCardPremium(
                service = service,
                onClick = { navController.navigate(Screen.ClientServiceDetails.createRoute(service.id)) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun ClientProfileHeader(user: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = user?.name ?: "Valued Client",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
        }

        AsyncImage(
            model = user?.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentScale = ContentScale.Crop,
            error = null // Placeholder or icon handled by background/fallback
        )
    }
}

@Composable
fun DashboardStatsSection(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            label = "Active",
            value = stats.activeProjectsCount.toString(),
            icon = Icons.Default.RocketLaunch,
            color = Color(0xFF6366F1),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "Pending",
            value = stats.pendingRequestsCount.toString(),
            icon = Icons.Default.PendingActions,
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "Invested",
            value = "$${stats.totalInvested.toInt()}",
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Request Service",
                icon = Icons.Default.AddBusiness,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate(Screen.ClientFreelanceServices.route) },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Support",
                icon = Icons.Default.SupportAgent,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { /* Navigate to Support */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: ImageVector, containerColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OngoingProjectsCarousel(projects: List<Freelance>, onProjectClick: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(projects) { project ->
            OngoingProjectCard(project = project, onClick = { onProjectClick(project.id) })
        }
    }
}

@Composable
fun OngoingProjectCard(project: Freelance, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkOutline, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(project.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(project.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Progress", style = MaterialTheme.typography.labelMedium)
                Text("${(project.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { project.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text("Oops! Something went wrong", style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { /* Retry logic could be added to ViewModel */ }) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        TextButton(onClick = onActionClick) {
            Text(actionText, fontWeight = FontWeight.Bold)
        }
    }
}
