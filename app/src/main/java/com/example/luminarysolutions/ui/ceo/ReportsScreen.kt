package com.example.luminarysolutions.ui.ceo

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

enum class ExportType { CSV, PDF }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {

    var fromDate by remember { mutableStateOf("2026-01-01") }
    var toDate by remember { mutableStateOf("2026-02-01") }
    var includeDonor by remember { mutableStateOf(true) }
    var includeProjects by remember { mutableStateOf(true) }

    var error by remember { mutableStateOf<String?>(null) }
    var previewText by remember { mutableStateOf("No preview yet. Tap “Generate Preview”.") }

    // Export state
    var pendingExport by remember { mutableStateOf<ExportType?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Launcher for "Save as..." file picker
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val type = pendingExport
        if (uri != null && type != null) {
            val ok = when (type) {
                ExportType.CSV -> writeTextFile(
                    context = navController.context,
                    uri = uri,
                    content = buildCsv(fromDate, toDate, includeDonor, includeProjects)
                )
                ExportType.PDF -> writeTextFile(
                    context = navController.context,
                    uri = uri,
                    content = buildPdfLikeText(fromDate, toDate, includeDonor, includeProjects)
                )
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (ok) "Export successful ✅" else "Export failed ❌"
                )
            }
        }
        pendingExport = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text("Generate a report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Choose date range and export format.", style = MaterialTheme.typography.bodyMedium)

            // Inputs card
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("From (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("To (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Include donor section")
                        Switch(checked = includeDonor, onCheckedChange = { includeDonor = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Include project breakdown")
                        Switch(checked = includeProjects, onCheckedChange = { includeProjects = it })
                    }

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Generate preview
            Button(
                onClick = {
                    val validation = validateDates(fromDate, toDate)
                    if (validation != null) {
                        error = validation
                        previewText = "No preview yet. Fix the error above."
                        return@Button
                    }
                    error = null
                    previewText = buildPreview(fromDate, toDate, includeDonor, includeProjects)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Preview")
            }

            // Export buttons (CSV + PDF)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedButton(
                    onClick = {
                        val validation = validateDates(fromDate, toDate)
                        if (validation != null) {
                            error = validation
                            scope.launch { snackbarHostState.showSnackbar("Fix date format first") }
                            return@OutlinedButton
                        }
                        error = null
                        pendingExport = ExportType.CSV
                        createDocumentLauncher.launch("finance_report_${fromDate}_to_${toDate}.csv")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export CSV")
                }

                Button(
                    onClick = {
                        val validation = validateDates(fromDate, toDate)
                        if (validation != null) {
                            error = validation
                            scope.launch { snackbarHostState.showSnackbar("Fix date format first") }
                            return@Button
                        }
                        error = null
                        pendingExport = ExportType.PDF
                        createDocumentLauncher.launch("finance_report_${fromDate}_to_${toDate}.pdf")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export PDF")
                }
            }

            // Preview card
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Preview", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(previewText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** --- Helpers --- **/

private fun validateDates(from: String, to: String): String? {
    val regex = Regex("""^\d{4}-\d{2}-\d{2}$""")
    if (!regex.matches(from) || !regex.matches(to)) {
        return "Date format must be YYYY-MM-DD (example: 2026-02-01)."
    }
    // Simple lexical compare works for YYYY-MM-DD
    if (from > to) return "From date cannot be later than To date."
    return null
}

private fun buildPreview(from: String, to: String, includeDonor: Boolean, includeProjects: Boolean): String {
    val sections = mutableListOf<String>()
    sections.add("Report Range: $from → $to")
    sections.add("Summary: Budget utilization, spend trends, approvals overview.")
    if (includeProjects) sections.add("Includes: Project breakdown (status, progress, spend).")
    if (includeDonor) sections.add("Includes: Donor / funder section (funds received, usage, compliance).")
    sections.add("Export ready: CSV or PDF.")
    return sections.joinToString("\n• ", prefix = "• ")
}

private fun buildCsv(from: String, to: String, includeDonor: Boolean, includeProjects: Boolean): String {
    // UI-only sample CSV content
    return buildString {
        appendLine("report_from,report_to,include_donor,include_projects")
        appendLine("$from,$to,$includeDonor,$includeProjects")
        appendLine()
        appendLine("section,item,value")
        appendLine("summary,total_budget,2000000")
        appendLine("summary,total_spent,1250000")
        appendLine("summary,pending_approvals,7")
        if (includeProjects) {
            appendLine("projects,clean_water_initiative,ongoing")
            appendLine("projects,school_renovation,completed")
        }
        if (includeDonor) {
            appendLine("donors,lumiSphere_fund,250000_received")
            appendLine("donors,partner_A,100000_received")
        }
    }
}

private fun buildPdfLikeText(from: String, to: String, includeDonor: Boolean, includeProjects: Boolean): String {
    // Placeholder “PDF” text (export flow works; real PDF later)
    return buildString {
        appendLine("LUMINARY HUB — FINANCE REPORT")
        appendLine("Range: $from to $to")
        appendLine()
        appendLine("SUMMARY")
        appendLine("- Total Budget: $2,000,000")
        appendLine("- Total Spent:  $1,250,000")
        appendLine("- Pending Approvals: 7")
        appendLine()
        if (includeProjects) {
            appendLine("PROJECT BREAKDOWN (Sample)")
            appendLine("- Clean Water Initiative: Ongoing")
            appendLine("- School Renovation: Completed")
            appendLine()
        }
        if (includeDonor) {
            appendLine("DONOR / FUNDER SECTION (Sample)")
            appendLine("- LumiSphere Fund: $250,000 received")
            appendLine("- Partner A: $100,000 received")
            appendLine()
        }
        appendLine("NOTE: This is a placeholder PDF export. Real PDF layout can be added later.")
    }
}

private fun writeTextFile(context: Context, uri: Uri, content: String): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray())
        }
        true
    } catch (e: Exception) {
        false
    }
}

@Preview
@Composable
fun ReportsScreenPreview() {
    ReportsScreen(navController = rememberNavController())
}
