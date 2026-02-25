package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDetailsScreen(
    navController: NavController,
    roleId: String
) {
    // UI-only: choose a permission template based on roleId
    val allPermissions = remember { permissionCatalog() }
    val selected = remember(roleId) {
        mutableStateListOf<String>().apply {
            addAll(
                when (roleId) {
                    "CEO" -> defaultCeoPermissions()
                    "IT_ADMIN" -> defaultITPermissions()
                    "VOLUNTEER" -> defaultVolunteerPermissions()
                    "DONOR" -> defaultDonorPermissions()
                    else -> emptyList()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Role: $roleId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            Text("Permissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Toggle what this role can do. (UI-only now; later we’ll save to Firestore.)",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allPermissions) { perm ->
                        val checked = perm in selected
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(perm, fontWeight = FontWeight.SemiBold)
                                Text(permHint(perm), style = MaterialTheme.typography.labelMedium)
                            }
                            Switch(
                                checked = checked,
                                onCheckedChange = { on ->
                                    if (on) selected.add(perm) else selected.remove(perm)
                                }
                            )
                        }
                        Divider()
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

/* ---------- Permission Catalog + Defaults ---------- */

private fun permissionCatalog(): List<String> = listOf(
    "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT",
    "FINANCE_VIEW", "FINANCE_APPROVE", "REPORTS_EXPORT",
    "PARTNERS_VIEW", "PARTNERS_ADD", "PARTNERS_EDIT",
    "COMMUNITY_VIEW", "BENEFICIARIES_MANAGE", "GRIEVANCES_MANAGE",
    "USERS_MANAGE", "ROLES_MANAGE", "AUDIT_LOGS_VIEW", "SYSTEM_SETTINGS_EDIT"
)

private fun permHint(p: String): String = when (p) {
    "PROJECTS_VIEW" -> "See projects and KPIs"
    "PROJECTS_CREATE" -> "Create new projects"
    "PROJECTS_EDIT" -> "Edit project details"
    "FINANCE_VIEW" -> "View budgets and expenses"
    "FINANCE_APPROVE" -> "Approve finance workflows"
    "REPORTS_EXPORT" -> "Export reports to PDF/CSV"
    "PARTNERS_VIEW" -> "View partner/donor list"
    "PARTNERS_ADD" -> "Add new partners/donors"
    "PARTNERS_EDIT" -> "Edit agreements and details"
    "COMMUNITY_VIEW" -> "View programs and community data"
    "BENEFICIARIES_MANAGE" -> "Register & manage beneficiaries"
    "GRIEVANCES_MANAGE" -> "Handle grievances & feedback"
    "USERS_MANAGE" -> "Create/edit/disable users"
    "ROLES_MANAGE" -> "Edit roles and permissions"
    "AUDIT_LOGS_VIEW" -> "View security/activity logs"
    "SYSTEM_SETTINGS_EDIT" -> "Security settings, session rules, etc."
    else -> ""
}

fun defaultCeoPermissions(): MutableList<String> = mutableStateListOf(
    "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT",
    "FINANCE_VIEW", "FINANCE_APPROVE", "REPORTS_EXPORT",
    "PARTNERS_VIEW", "PARTNERS_ADD", "PARTNERS_EDIT",
    "COMMUNITY_VIEW", "BENEFICIARIES_MANAGE", "GRIEVANCES_MANAGE"
    // ✅ CEO does NOT get technical admin perms
)

fun defaultITPermissions(): MutableList<String> = mutableStateListOf(
    "USERS_MANAGE", "ROLES_MANAGE", "AUDIT_LOGS_VIEW", "SYSTEM_SETTINGS_EDIT"
)

fun defaultVolunteerPermissions(): MutableList<String> = mutableStateListOf(
    "PROJECTS_VIEW", "COMMUNITY_VIEW"
)

fun defaultDonorPermissions(): MutableList<String> = mutableStateListOf(
    "REPORTS_EXPORT", "PARTNERS_VIEW"
)

@Preview
@Composable
fun RoleDetailsScreenPreview() {
    RoleDetailsScreen(navController = rememberNavController(), roleId = "CEO")
}