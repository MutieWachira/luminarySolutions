package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.navigation.Screen
import java.util.UUID

private val COL_ROLE = 220.dp
private val COL_DESC = 320.dp
private val COL_PERMS = 140.dp
private val ROW_H = 52.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesScreen(navController: NavController) {

    // UI-only roles store (later Firestore)
    val roles = remember {
        mutableStateListOf(
            RoleUi("CEO", "Full business access (no technical admin tools)", defaultCeoPermissions()),
            RoleUi("IT_ADMIN", "Identity, security, logs, settings, user management", defaultITPermissions()),
            RoleUi("VOLUNTEER", "Field tasks, updates, basic views", defaultVolunteerPermissions()),
            RoleUi("DONOR", "View impact reports, donor dashboard", defaultDonorPermissions())
        )
    }

    val border = MaterialTheme.colorScheme.outlineVariant
    val headerBg = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roles & Permissions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        // add new role (UI-only)
                        val newId = UUID.randomUUID().toString()
                        roles.add(
                            0,
                            RoleUi("NEW_ROLE", "Describe this role...", mutableStateListOf())
                        )
                    }) {
                        Text("Add Role")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            border = BorderStroke(1.dp, border)
        ) {
            LazyColumn {

                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ROW_H)
                            .background(headerBg)
                    ) {
                        HeaderCell("Role", COL_ROLE, border)
                        HeaderCell("Description", COL_DESC, border)
                        HeaderCell("Permissions", COL_PERMS, border)
                    }
                }

                items(roles, key = { it.key }) { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ROW_H)
                            .clickable {
                                navController.navigate(Screen.RoleDetails.createRoute(r.key))
                            }
                    ) {
                        DataCell(r.key, COL_ROLE, border, bold = true)
                        DataCell(r.description, COL_DESC, border)
                        DataCell(r.permissions.size.toString(), COL_PERMS, border)
                    }
                    Divider(color = border)
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Tap a role to edit its permissions (UI-only for now).",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, w: Dp, border: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .width(w)
            .fillMaxHeight()
            .border(1.dp, border)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DataCell(
    text: String,
    w: Dp,
    border: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(w)
            .fillMaxHeight()
            .border(1.dp, border)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}

data class RoleUi(
    val key: String, // ex: "CEO"
    val description: String,
    val permissions: MutableList<String>
)

@Preview
@Composable
fun RolesScreenPreview() {
    RolesScreen(navController = rememberNavController())
}