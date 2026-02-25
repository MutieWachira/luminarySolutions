package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val COL_TIME = 170.dp
private val COL_ACTOR = 170.dp
private val COL_ACTION = 230.dp
private val COL_TARGET = 230.dp
private val COL_RESULT = 130.dp
private val ROW_H = 52.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(navController: NavController) {

    // UI-only logs (later: Firestore)
    val logs = remember { sampleAuditLogs().toMutableStateList() }

    var search by remember { mutableStateOf("") }
    var sortCol by remember { mutableStateOf(SortCol.TIME) }
    var sortDir by remember { mutableStateOf(SortDir.DESC) } // newest first by default

    var filterResult by remember { mutableStateOf(ResultFilter.ALL) }

    // pagination
    var pageSize by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }

    // details
    var selected by remember { mutableStateOf<AuditLogUi?>(null) }

    val border = MaterialTheme.colorScheme.outlineVariant
    val headerBg = MaterialTheme.colorScheme.surfaceVariant
    val zebra1 = MaterialTheme.colorScheme.surface
    val zebra2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)

    val hScroll = rememberScrollState()

    // 1) filter/search
    val filtered = remember(logs, search, filterResult) {
        val q = search.trim()
        logs.filter { l ->
            val matchesQuery = q.isBlank() ||
                    l.actor.contains(q, true) ||
                    l.action.contains(q, true) ||
                    l.target.contains(q, true) ||
                    l.result.name.contains(q, true)

            val matchesResult = when (filterResult) {
                ResultFilter.ALL -> true
                ResultFilter.SUCCESS -> l.result == Result.SUCCESS
                ResultFilter.FAIL -> l.result == Result.FAIL
            }
            matchesQuery && matchesResult
        }
    }

    // 2) sort
    val sorted = remember(filtered, sortCol, sortDir) {
        val cmp = when (sortCol) {
            SortCol.TIME -> compareBy<AuditLogUi> { it.timestamp }
            SortCol.ACTOR -> compareBy { it.actor.lowercase() }
            SortCol.ACTION -> compareBy { it.action.lowercase() }
            SortCol.TARGET -> compareBy { it.target.lowercase() }
            SortCol.RESULT -> compareBy { it.result.name }
            else -> compareBy<AuditLogUi> { it.timestamp }.reversed()
        }
        val base = filtered.sortedWith(cmp)
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
                title = { Text("Audit Logs") },
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
                            page = 1
                        },
                        placeholder = { Text("Search logs...") },
                        singleLine = true,
                        modifier = Modifier.width(220.dp)
                    )
                    Spacer(Modifier.width(10.dp))
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

            // Filters + stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterResult == ResultFilter.ALL,
                        onClick = { filterResult = ResultFilter.ALL; page = 1 },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = filterResult == ResultFilter.SUCCESS,
                        onClick = { filterResult = ResultFilter.SUCCESS; page = 1 },
                        label = { Text("Success") }
                    )
                    FilterChip(
                        selected = filterResult == ResultFilter.FAIL,
                        onClick = { filterResult = ResultFilter.FAIL; page = 1 },
                        label = { Text("Failed") }
                    )
                }

                // quick stats
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssistChip(onClick = { }, label = { Text("Total: ${logs.size}") })
                    AssistChip(onClick = { }, label = { Text("Today: ${logs.count { it.isToday() }}") })
                }
            }

            // Pagination toolbar
            TablePager(
                totalItems = totalItems,
                page = page,
                totalPages = totalPages,
                pageSize = pageSize,
                onPageSizeChange = { ps -> pageSize = ps; page = 1 },
                onPrev = { page = (page - 1).coerceAtLeast(1) },
                onNext = { page = (page + 1).coerceAtMost(totalPages) }
            )

            // Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, border)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {

                    stickyHeader {
                        AuditHeaderRow(
                            borderColor = border,
                            background = headerBg,
                            horizontalScroll = hScroll,
                            sortCol = sortCol,
                            sortDir = sortDir
                        ) { col ->
                            if (sortCol == col) {
                                sortDir = if (sortDir == SortDir.ASC) SortDir.DESC else SortDir.ASC
                            } else {
                                sortCol = col
                                sortDir = SortDir.ASC
                            }
                            page = 1
                        }
                    }

                    if (paged.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No logs match your filters.")
                            }
                        }
                    } else {
                        items(paged, key = { it.id }) { log ->
                            val index = paged.indexOf(log)
                            val bg = if (index % 2 == 0) zebra1 else zebra2

                            AuditRow(
                                log = log,
                                background = bg,
                                borderColor = border,
                                horizontalScroll = hScroll,
                                onClick = { selected = log }
                            )
                        }
                    }
                }
            }

            // Pagination toolbar bottom
            TablePager(
                totalItems = totalItems,
                page = page,
                totalPages = totalPages,
                pageSize = pageSize,
                onPageSizeChange = { ps -> pageSize = ps; page = 1 },
                onPrev = { page = (page - 1).coerceAtLeast(1) },
                onNext = { page = (page + 1).coerceAtMost(totalPages) }
            )
        }
    }

    // Details dialog
    if (selected != null) {
        AuditLogDetailsDialog(log = selected!!, onDismiss = { selected = null })
    }
}

/* ---------------- Table UI ---------------- */

@Composable
private fun AuditHeaderRow(
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
            .height(ROW_H)
            .background(background)
            .horizontalScroll(horizontalScroll)
    ) {
        HeaderCellSortable("Time", COL_TIME, borderColor, sortCol == SortCol.TIME, sortDir) { onSort(SortCol.TIME) }
        HeaderCellSortable("Actor", COL_ACTOR, borderColor, sortCol == SortCol.ACTOR, sortDir) { onSort(SortCol.ACTOR) }
        HeaderCellSortable("Action", COL_ACTION, borderColor, sortCol == SortCol.ACTION, sortDir) { onSort(SortCol.ACTION) }
        HeaderCellSortable("Target", COL_TARGET, borderColor, sortCol == SortCol.TARGET, sortDir) { onSort(SortCol.TARGET) }
        HeaderCellSortable("Result", COL_RESULT, borderColor, sortCol == SortCol.RESULT, sortDir) { onSort(SortCol.RESULT) }
    }
}

@Composable
private fun AuditRow(
    log: AuditLogUi,
    background: Color,
    borderColor: Color,
    horizontalScroll: androidx.compose.foundation.ScrollState,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_H)
            .background(background)
            .horizontalScroll(horizontalScroll)
            .padding(end = 2.dp)
    ) {
        DataCell(log.timeText(), COL_TIME, borderColor, onClick)
        DataCell(log.actor, COL_ACTOR, borderColor, onClick)
        DataCell(log.action, COL_ACTION, borderColor, onClick)
        DataCell(log.target, COL_TARGET, borderColor, onClick)
        ResultCell(log.result, COL_RESULT, borderColor, onClick)
    }
}

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
            .padding(horizontal = 12.dp)
            .then(Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (active) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = if (dir == SortDir.ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(Modifier.width(6.dp))
                TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Sort", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun DataCell(
    text: String,
    width: Dp,
    borderColor: Color,
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
        Text(text, maxLines = 1)
    }
}

@Composable
private fun ResultCell(
    result: Result,
    width: Dp,
    borderColor: Color,
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
        val label = if (result == Result.SUCCESS) "Success" else "Failed"
        val colors = if (result == Result.SUCCESS) {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                labelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        AssistChip(onClick = { }, label = { Text(label) }, colors = colors)
    }
}

/* ---------------- Pager ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablePager(
    totalItems: Int,
    page: Int,
    totalPages: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val sizes = listOf(5, 10, 20, 50)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Rows: $totalItems", style = MaterialTheme.typography.labelLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = !open }) {
                OutlinedTextField(
                    value = "Page size: $pageSize",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.menuAnchor().width(160.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) }
                )
                ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    sizes.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.toString()) },
                            onClick = { onPageSizeChange(s); open = false }
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

/* ---------------- Details ---------------- */

@Composable
private fun AuditLogDetailsDialog(log: AuditLogUi, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audit Log Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Time", log.timeText())
                DetailRow("Actor", log.actor)
                DetailRow("Action", log.action)
                DetailRow("Target", log.target)
                DetailRow("Result", log.result.name)
                DetailRow("IP", log.ip)
                DetailRow("Device", log.device)
                DetailRow("Session", log.sessionId)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ---------------- Model + Sample Data ---------------- */

private enum class Result { SUCCESS, FAIL }
private enum class ResultFilter { ALL, SUCCESS, FAIL }

private data class AuditLogUi(
    val id: String,
    val timestamp: Long,
    val actor: String,
    val action: String,
    val target: String,
    val result: Result,
    val ip: String,
    val device: String,
    val sessionId: String
)

private fun AuditLogUi.timeText(): String {
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return fmt.format(Date(timestamp))
}

private fun AuditLogUi.isToday(): Boolean {
    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L
    return (now - timestamp) < dayMs
}

private fun sampleAuditLogs(): List<AuditLogUi> {
    val now = System.currentTimeMillis()
    fun log(minsAgo: Int, actor: String, action: String, target: String, ok: Boolean) =
        AuditLogUi(
            id = UUID.randomUUID().toString(),
            timestamp = now - minsAgo * 60_000L,
            actor = actor,
            action = action,
            target = target,
            result = if (ok) Result.SUCCESS else Result.FAIL,
            ip = "197.232.12.${(10..99).random()}",
            device = "Android • Pixel",
            sessionId = UUID.randomUUID().toString().take(8)
        )

    return listOf(
        log(5, "IT Admin", "LOGIN", "Auth", true),
        log(12, "IT Admin", "UPDATE_ROLE", "User: volunteer1@luminary.com", true),
        log(25, "CEO Account", "EXPORT_REPORT", "Reports: Q1 Impact", true),
        log(40, "IT Admin", "DISABLE_USER", "User: donor1@luminary.com", true),
        log(55, "Unknown", "LOGIN", "Auth", false),
        log(80, "IT Admin", "CHANGE_SETTINGS", "Session Timeout", true),
        log(120, "CEO Account", "VIEW_FINANCE", "Finance Dashboard", true),
        log(170, "Unknown", "ACCESS_DENIED", "Admin Panel", false),
    )
}
@Preview
@Composable
fun AuditLogsSCreenPreview(){
    AuditLogsScreen(rememberNavController())
}