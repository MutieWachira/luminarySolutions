package com.example.luminarysolutions.ui.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.luminarysolutions.data.models.User
import com.example.luminarysolutions.ui.auth.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val roleFilter by viewModel.roleFilter.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("User Directory", fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        BadgedBox(badge = {
                            if (roleFilter != null) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary)
                            }
                        }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Search Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search by name, email, or role") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // User List
            if (users.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(16.dp))
                        Text("No users found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(1.dp), // Divider effect
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(users) { user ->
                        UserRowItem(
                            user = user,
                            onToggleEnabled = { viewModel.toggleUserEnabled(user) },
                            onRoleChange = { newRole -> viewModel.updateUserRole(user.id, newRole) }
                        )
                    }
                }
            }
        }

        // Filter Dialog
        if (showFilterMenu) {
            RoleFilterBottomSheet(
                currentFilter = roleFilter,
                onDismiss = { showFilterMenu = false },
                onRoleSelected = { 
                    viewModel.updateRoleFilter(it)
                    showFilterMenu = false
                }
            )
        }
    }
}

@Composable
fun UserRowItem(
    user: User,
    onToggleEnabled: () -> Unit,
    onRoleChange: (UserRole) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Optional: Details */ },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image with Status Indicator
            Box {
                AsyncImage(
                    model = user.profileImageUrl ?: "https://ui-avatars.com/api/?name=${user.name}&background=random",
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (user.enabled) Color(0xFF10B981) else Color(0xFFEF4444))
                        .align(Alignment.BottomEnd)
                        .background(MaterialTheme.colorScheme.surface) // Border effect
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (user.enabled) Color(0xFF10B981) else Color(0xFFEF4444))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (user.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Mail, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SuggestionChip(
                    onClick = { showRoleDialog = true },
                    label = { Text(user.role.name, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = MaterialTheme.colorScheme.primary,
                        iconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = user.enabled,
                    onCheckedChange = { onToggleEnabled() },
                    scale = 0.8f // Custom scaling if needed
                )
                Text(
                    text = if (user.enabled) "Active" else "Disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.enabled) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }

    if (showRoleDialog) {
        RolePickerSecondary(
            currentRole = user.role,
            onDismiss = { showRoleDialog = false },
            onRoleSelected = { 
                onRoleChange(it)
                showRoleDialog = false
            }
        )
    }
}

// Helper to scale switch
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Box(modifier = modifier) {
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = (52 * scale).dp, height = (32 * scale).dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleFilterBottomSheet(
    currentFilter: UserRole?,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Filter by Role", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Surface(
                onClick = { onRoleSelected(null) },
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                color = if (currentFilter == null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
            ) {
                ListItem(
                    headlineContent = { Text("All Users") },
                    leadingContent = { RadioButton(selected = currentFilter == null, onClick = { onRoleSelected(null) }) }
                )
            }
            
            UserRole.values().filter { it != UserRole.UNKNOWN }.forEach { role ->
                Surface(
                    onClick = { onRoleSelected(role) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = if (role == currentFilter) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                ) {
                    ListItem(
                        headlineContent = { Text(role.name) },
                        leadingContent = { RadioButton(selected = role == currentFilter, onClick = { onRoleSelected(role) }) }
                    )
                }
            }
        }
    }
}

@Composable
fun RolePickerSecondary(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign New Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.values().filter { it != UserRole.UNKNOWN }.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (role == currentRole) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = role == currentRole, onClick = { onRoleSelected(role) })
                        Text(role.name, modifier = Modifier.padding(start = 12.dp), fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
