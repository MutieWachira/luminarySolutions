package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.auth.UserRole
import java.util.UUID

private val COL_NAME = 180.dp   // sticky column
private val COL_EMAIL = 200.dp
private val COL_ROLE = 160.dp
private val COL_STATUS = 150.dp
private val ROW_HEIGHT = 52.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(navController: NavController, role: UserRole) {

    // UI-only list (later Firestore)
    val users = remember {
        mutableStateListOf(
            UserUi(UUID.randomUUID().toString(), "CEO Account", "ceo@luminary.com", UserRole.CEO, true),
            UserUi(UUID.randomUUID().toString(), "IT Admin", "itadmin@luminary.com", UserRole.ADMIN, true),
            UserUi(UUID.randomUUID().toString(), "Volunteer 1", "volunteer1@luminary.com", UserRole.VOLUNTEER, true),
            UserUi(UUID.randomUUID().toString(), "Donor 1", "donor1@luminary.com", UserRole.DONOR, false),
            UserUi(UUID.randomUUID().toString(), "Volunteer 2", "volunteer2@luminary.com", UserRole.VOLUNTEER, true),
            UserUi(UUID.randomUUID().toString(), "Donor 2", "donor2@luminary.com", UserRole.DONOR, true),
            UserUi(UUID.randomUUID().toString(), "Staff A", "staffA@luminary.com", UserRole.ADMIN, true),
            UserUi(UUID.randomUUID().toString(), "Staff B", "staffB@luminary.com", UserRole.ADMIN, false),
        )
    }

    var search by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<UserUi?>(null) }

    // sorting
    var sortCol by remember { mutableStateOf(SortCol.NAME) }
    var sortDir by remember { mutableStateOf(SortDir.ASC) }

    // pagination
    var pageSize by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }

    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val headerBg = MaterialTheme.colorScheme.surfaceVariant
    val zebra1 = MaterialTheme.colorScheme.surface
    val zebra2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)

    val listState = rememberLazyListState()
    val horizontalScroll = rememberScrollState()

    // 1) filter
    val filtered = remember(users, search) {
        val q = search.trim()
        if (q.isBlank()) users.toList()
        else users.filter {
            it.name.contains(q, true) ||
                    it.email.contains(q, true) ||
                    it.role.name.contains(q, true) ||
                    (if (it.enabled) "active" else "disabled").contains(q, true)
        }
    }

    // 2) sort
    val sorted = remember(filtered, sortCol, sortDir) {
        val comparator = when (sortCol) {
            SortCol.NAME -> compareBy<UserUi> { it.name.lowercase() }
            SortCol.EMAIL -> compareBy { it.email.lowercase() }
            SortCol.ROLE -> compareBy { it.role.name }
            SortCol.STATUS -> compareBy { if (it.enabled) 0 else 1 } // Active first
            else -> compareBy { it.name.lowercase() } // Default sort
        }
        val base = filtered.sortedWith(comparator)
        if (sortDir == SortDir.ASC) base else base.asReversed()
    }

    // 3) pagination
    val totalItems = sorted.size
    val totalPages = remember(totalItems, pageSize) {
        val ps = pageSize.coerceAtLeast(1)
        ((totalItems + ps - 1) / ps).coerceAtLeast(1)
    }
    LaunchedEffect(totalPages) {
        if (page > totalPages) page = totalPages
        if (page < 1) page = 1
    }
    val paged = remember(sorted, page, pageSize) {
        val ps = pageSize.coerceAtLeast(1)
        val start = ((page - 1) * ps).coerceAtLeast(0)
        val end = (start + ps).coerceAtMost(sorted.size)
        if (start >= end) emptyList() else sorted.subList(start, end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    OutlinedTextField(
                        value = search,
                        onValueChange = {
                            search = it
                            page = 1 // reset page on search
                        },
                        placeholder = { Text("Search...") },
                        singleLine = true,
                        modifier = Modifier.width(220.dp)
                    )
                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = { selectedUser = UserUi("","","", UserRole.VOLUNTEER,true)}
                    ){
                        Text("Add User")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // quick stats
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniStat("Total", users.size.toString(), Modifier.weight(1f))
                MiniStat("Active", users.count { it.enabled }.toString(), Modifier.weight(1f))
                MiniStat("Disabled", users.count { !it.enabled }.toString(), Modifier.weight(1f))
            }

            // Pagination controls (top)
            EnterpriseTableToolbar(
                totalItems = totalItems,
                page = page,
                totalPages = totalPages,
                pageSize = pageSize,
                onPageSizeChange = { newSize ->
                    pageSize = newSize
                    page = 1
                },
                onPrev = { page = (page - 1).coerceAtLeast(1) },
                onNext = { page = (page + 1).coerceAtMost(totalPages) }
            )

            // Table container
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, borderColor)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // ✅ Sticky header
                    stickyHeader {
                        HeaderRow(
                            borderColor = borderColor,
                            background = headerBg,
                            horizontalScroll = horizontalScroll,
                            sortCol = sortCol,
                            sortDir = sortDir,
                            onSort = { col ->
                                if (sortCol == col) {
                                    sortDir = if (sortDir == SortDir.ASC) SortDir.DESC else SortDir.ASC
                                } else {
                                    sortCol = col
                                    sortDir = SortDir.ASC
                                }
                                page = 1
                            }
                        )
                    }

                    if (paged.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No users found.")
                            }
                        }
                    } else {
                        items(paged, key = { it.id }) { user ->
                            val index = paged.indexOf(user)
                            val rowBg = if (index % 2 == 0) zebra1 else zebra2

                            DataRow(
                                user = user,
                                background = rowBg,
                                borderColor = borderColor,
                                horizontalScroll = horizontalScroll,
                                onNameClick = { selectedUser = user }
                            )
                        }
                    }
                }
            }

            // Pagination controls (bottom)
            EnterpriseTableToolbar(
                totalItems = totalItems,
                page = page,
                totalPages = totalPages,
                pageSize = pageSize,
                onPageSizeChange = { newSize ->
                    pageSize = newSize
                    page = 1
                },
                onPrev = { page = (page - 1).coerceAtLeast(1) },
                onNext = { page = (page + 1).coerceAtMost(totalPages) }
            )

            Text(
                "Tip: Click a name to edit details (UI-only for now).",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (selectedUser != null) {
        val isNew = selectedUser!!.id.isBlank()
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = { selectedUser = null },
            onSave = { updated ->
                if (isNew) {
                    users.add(
                        0, updated.copy(id = UUID.randomUUID().toString())
                    )
                } else {
                    val idx = users.indexOfFirst { it.id == updated.id }
                    if (idx != -1) users[idx] = updated
                    selectedUser = null
                }
            }
        )
    }
}

/* ---------------- Toolbar ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterpriseTableToolbar(
    totalItems: Int,
    page: Int,
    totalPages: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val sizes = listOf(5, 10, 20, 50)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Showing ${if (totalItems == 0) 0 else 1}–$totalItems users",
            style = MaterialTheme.typography.labelLarge
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            // Page size
            ExposedDropdownMenuBox(
                expanded = menuOpen,
                onExpandedChange = { menuOpen = !menuOpen }
            ) {
                OutlinedTextField(
                    value = "Rows: $pageSize",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.menuAnchor().width(130.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) }
                )
                ExposedDropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    sizes.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.toString()) },
                            onClick = {
                                onPageSizeChange(s)
                                menuOpen = false
                            }
                        )
                    }
                }
            }

            Text("Page $page / $totalPages", style = MaterialTheme.typography.labelLarge)

            OutlinedButton(onClick = onPrev, enabled = page > 1) { Text("Prev") }
            Button(onClick = onNext, enabled = page < totalPages) { Text("Next") }
        }
    }
}

/* ---------------- Table Header + Rows ---------------- */

@Composable
private fun HeaderRow(
    borderColor: Color,
    background: Color,
    horizontalScroll: androidx.compose.foundation.ScrollState,
    sortCol: SortCol,
    sortDir: SortDir,
    onSort: (SortCol) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .background(background)
    ) {
        // ✅ Sticky Name column (NOT inside horizontal scroll)
        HeaderCellSortable(
            text = "Name",
            width = COL_NAME,
            borderColor = borderColor,
            active = sortCol == SortCol.NAME,
            dir = sortDir,
            onClick = { onSort(SortCol.NAME) }
        )

        // ✅ Everything else scrolls horizontally
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            HeaderCellSortable(
                text = "Email",
                width = COL_EMAIL,
                borderColor = borderColor,
                active = sortCol == SortCol.EMAIL,
                dir = sortDir,
                onClick = { onSort(SortCol.EMAIL) }
            )
            HeaderCellSortable(
                text = "Role",
                width = COL_ROLE,
                borderColor = borderColor,
                active = sortCol == SortCol.ROLE,
                dir = sortDir,
                onClick = { onSort(SortCol.ROLE) }
            )
            HeaderCellSortable(
                text = "Status",
                width = COL_STATUS,
                borderColor = borderColor,
                active = sortCol == SortCol.STATUS,
                dir = sortDir,
                onClick = { onSort(SortCol.STATUS) }
            )
        }
    }
}

@Composable
private fun DataRow(
    user: UserUi,
    background: Color,
    borderColor: Color,
    horizontalScroll: androidx.compose.foundation.ScrollState,
    onNameClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .background(background)
    ) {
        // ✅ Sticky Name cell
        DataCell(
            text = user.name,
            width = COL_NAME,
            borderColor = borderColor,
            isClickable = true,
            onClick = onNameClick
        )

        // ✅ Scrollable part
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            DataCell(user.email, COL_EMAIL, borderColor)

            DataCell(user.role.name, COL_ROLE, borderColor)

            Box(
                modifier = Modifier
                    .width(COL_STATUS)
                    .fillMaxHeight()
                    .border(1.dp, borderColor)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                StatusChip(enabled = user.enabled)
            }
        }
    }
}

/* ---------------- Cells ---------------- */

@Composable
private fun HeaderCellSortable(
    text: String,
    width: Dp,
    borderColor: Color,
    active: Boolean,
    dir: SortDir,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .border(1.dp, borderColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            if (active) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = if (dir == SortDir.ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun DataCell(
    text: String,
    width: Dp,
    borderColor: Color,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val base = Modifier
        .width(width)
        .fillMaxHeight()
        .border(1.dp, borderColor)
        .padding(horizontal = 12.dp)

    val clickable = if (isClickable && onClick != null) base.clickable { onClick() } else base

    Box(modifier = clickable, contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun StatusChip(enabled: Boolean) {
    val label = if (enabled) "Active" else "Disabled"
    val colors = if (enabled) {
        AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    } else {
        AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onErrorContainer,
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    AssistChip(
        onClick = { },
        label = { Text(label) },
        colors = colors
    )
}

/* ---------------- Stats + Edit Dialog ---------------- */

@Composable
private fun MiniStat(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: UserUi,
    onDismiss: () -> Unit,
    onSave: (UserUi) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var enabled by remember { mutableStateOf(user.enabled) }

    var selectedRole by remember { mutableStateOf(user.role) }
    var roleMenuOpen by remember { mutableStateOf(false) }

    val canSave = name.trim().isNotBlank() && email.trim().contains("@")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = roleMenuOpen,
                    onExpandedChange = { roleMenuOpen = !roleMenuOpen }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = roleMenuOpen,
                        onDismissRequest = { roleMenuOpen = false }
                    ) {
                        listOf(UserRole.CEO, UserRole.ADMIN, UserRole.VOLUNTEER, UserRole.DONOR).forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = {
                                    selectedRole = r
                                    roleMenuOpen = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                    Text(if (enabled) "Active" else "Disabled")
                }

                Text(
                    text = "UI-only editing for now. Next step: connect Firestore.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        user.copy(
                            name = name.trim(),
                            email = email.trim(),
                            role = selectedRole,
                            enabled = enabled
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ---------------- Model ---------------- */

private data class UserUi(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val enabled: Boolean
)

@Preview(showBackground = true)
@Composable
fun UsersScreenPreview() {
    UsersScreen(
        navController = rememberNavController(),
        role = UserRole.ADMIN
    )
}