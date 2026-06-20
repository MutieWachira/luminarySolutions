package com.example.luminarysolutions.ui.ceo

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.data.models.Donor
import com.example.luminarysolutions.ui.navigation.Screen

/**
 * Production-ready Donors Screen.
 * Implements:
 * 1. Clean Architecture with MVVM
 * 2. Real-time Firestore connection via Repository
 * 3. Modern Swipe-to-Action (Edit/Delete)
 * 4. Paginated data loading for performance
 * 5. Interactive UI transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen(
    navController: NavController,
    viewModel: DonorsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var donorToEdit by remember { mutableStateOf<Donor?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Pagination trigger: detect when user scrolls near the bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // Load more when 2 items from the bottom
            lastVisibleItemIndex >= totalItemsCount - 2 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.canLoadMore && !uiState.isPaginating) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(targetState = isSearching, label = "TitleSearchTransition") { searching ->
                        if (searching) {
                            TextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Search donors...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                "Donors",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearching = !isSearching 
                        if (!isSearching) viewModel.onSearchQueryChange("")
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    donorToEdit = null
                    showAddDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Donor", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Stats Header
            DonorStatsHeader(uiState.donors)

            // Filtering Section
            DonorFilterRow(
                selectedFilter = uiState.filter,
                onFilterSelected = { viewModel.onFilterChange(it) }
            )

            if (uiState.isLoading && uiState.donors.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.donors.isEmpty()) {
                EmptyDonorsState(
                    isSearching = uiState.searchQuery.isNotEmpty(),
                    onAddClick = { 
                        donorToEdit = null
                        showAddDialog = true 
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.donors, key = { it.id }) { donor ->
                        SwipeableDonorCard(
                            donor = donor,
                            onEdit = {
                                donorToEdit = donor
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteDonor(donor.id)
                            },
                            onClick = {
                                navController.navigate(Screen.DonorDetails.createRoute(donor.id))
                            }
                        )
                    }
                    
                    // Loading indicator for pagination
                    if (uiState.isPaginating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditDonorDialog(
            donor = donorToEdit,
            onDismiss = { 
                showAddDialog = false
                donorToEdit = null
            },
            onSave = { name, status, value ->
                if (donorToEdit == null) {
                    viewModel.addDonor(name, status, value)
                } else {
                    viewModel.updateDonor(donorToEdit!!.copy(name = name, status = status, valueOrNote = value))
                }
                showAddDialog = false
                donorToEdit = null
            }
        )
    }
}

/**
 * Modern Swipe-to-Dismiss implementation for item actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableDonorCard(
    donor: Donor,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // Return false to snap back
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true // Return true to dismiss
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF3B82F6) // Edit Blue
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF43F5E) // Delete Red
                    else -> Color.Transparent
                }, label = "BackgroundColor"
            )
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1.25f,
                label = "IconScale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(28.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            DonorCard(donor = donor, onClick = onClick)
        },
        modifier = Modifier.animateContentSize()
    )
}

@Composable
fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DonorStatsHeader(donors: List<Donor>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            label = "Active Donors",
            value = donors.count { it.status == "Active" }.toString(),
            icon = Icons.Default.Favorite,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "Total Reach",
            value = donors.size.toString(),
            icon = Icons.Default.Groups,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DonorFilterRow(
    selectedFilter: DonorFilter,
    onFilterSelected: (DonorFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(DonorFilter.entries.toTypedArray()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = null
            )
        }
    }
}

@Composable
fun DonorCard(donor: Donor, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = getDonorStatusColor(donor.status).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = getDonorStatusColor(donor.status),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    donor.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Text(
                    donor.valueOrNote,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        donor.lastContact,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            DonorStatusBadge(donor.status)
        }
    }
}

@Composable
fun DonorStatusBadge(status: String) {
    val color = getDonorStatusColor(status)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun getDonorStatusColor(status: String): Color = when (status) {
    "Active" -> Color(0xFF10B981)
    "Inactive" -> Color(0xFFF43F5E)
    "Pending" -> Color(0xFFF59E0B)
    else -> Color(0xFF6B7280)
}

@Composable
fun EmptyDonorsState(isSearching: Boolean, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.PeopleOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (isSearching) "No matching donors" else "Your donor list is empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isSearching) "Try adjusting your search or filters" else "Start by adding your first donor to the system.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!isSearching) {
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Your First Donor", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDonorDialog(
    donor: Donor? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(donor?.name ?: "") }
    var valueOrNote by remember { mutableStateOf(donor?.valueOrNote ?: "") }
    var status by remember { mutableStateOf(donor?.status ?: "Active") }
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Pending", "Inactive")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(name, status, valueOrNote) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (donor == null) "Save Donor" else "Update Donor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (donor == null) "Add New Donor" else "Edit Donor", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Donor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = valueOrNote,
                    onValueChange = { valueOrNote = it },
                    label = { Text("Contribution / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DonorsScreenPreview() {
    DonorsScreen(rememberNavController())
}
