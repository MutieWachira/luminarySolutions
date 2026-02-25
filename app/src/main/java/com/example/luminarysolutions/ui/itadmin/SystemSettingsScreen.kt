package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Production-ready UI screen.
 * For now it uses an in-memory store (SettingsStore).
 * Later, replace SettingsStore.load/save with Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController) {

    // load current settings once
    var settings by remember { mutableStateOf(SettingsStore.load()) }
    var original by remember { mutableStateOf(settings) }

    var saving by remember { mutableStateOf(false) }
    var snack by remember { mutableStateOf<String?>(null) }

    val hasChanges = settings != original

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { settings = original },
                        enabled = hasChanges && !saving
                    ) { Text("Reset") }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            saving = true
                            // simulate save (replace with async Firestore later)
                            SettingsStore.save(settings)
                            original = settings
                            saving = false
                            snack = "Settings saved."
                        },
                        enabled = hasChanges && !saving
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (saving) "Saving..." else "Save changes")
                    }

                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (settings.maintenanceMode) {
                MaintenanceBanner()
            }

            // Security section
            SectionCard(
                title = "Security",
                subtitle = "Authentication, sessions and protection controls."
            ) {
                SettingRow(
                    title = "Require MFA for privileged roles",
                    subtitle = "For CEO + IT Admin accounts (recommended).",
                    trailing = {
                        Switch(
                            checked = settings.mfaRequiredForPrivileged,
                            onCheckedChange = { settings = settings.copy(mfaRequiredForPrivileged = it) }
                        )
                    }
                )

                Divider()

                SessionTimeoutRow(
                    minutes = settings.sessionTimeoutMinutes,
                    onChange = { settings = settings.copy(sessionTimeoutMinutes = it) }
                )

                Divider()

                SettingRow(
                    title = "Maintenance Mode",
                    subtitle = "Blocks normal users. Admins can still access the system.",
                    trailing = {
                        Switch(
                            checked = settings.maintenanceMode,
                            onCheckedChange = { settings = settings.copy(maintenanceMode = it) }
                        )
                    }
                )
            }

            // Password policy section
            SectionCard(
                title = "Password Policy",
                subtitle = "Controls for strong passwords."
            ) {
                MinPasswordLengthRow(
                    value = settings.minPasswordLength,
                    onChange = { settings = settings.copy(minPasswordLength = it) }
                )

                Divider()

                SettingRow(
                    title = "Require special character",
                    subtitle = "Example: ! @ # $ % (recommended).",
                    trailing = {
                        Switch(
                            checked = settings.requireSpecialChar,
                            onCheckedChange = { settings = settings.copy(requireSpecialChar = it) }
                        )
                    }
                )
            }

            // System section
            SectionCard(
                title = "System",
                subtitle = "Platform operational settings."
            ) {
                SettingRow(
                    title = "Audit logging",
                    subtitle = "Keep detailed activity logs for accountability.",
                    trailing = {
                        Switch(
                            checked = settings.auditLoggingEnabled,
                            onCheckedChange = { settings = settings.copy(auditLoggingEnabled = it) }
                        )
                    }
                )

                Divider()

                SettingRow(
                    title = "Email notifications",
                    subtitle = "Send system alerts to admins.",
                    trailing = {
                        Switch(
                            checked = settings.emailAlertsEnabled,
                            onCheckedChange = { settings = settings.copy(emailAlertsEnabled = it) }
                        )
                    }
                )
            }

            // Footer hint
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "These settings are UI-only now. Next, we’ll connect them to Firestore so they persist across devices.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/* ---------------- UI Components ---------------- */

@Composable
private fun MaintenanceBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Maintenance Mode is ON", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    "Normal users cannot access the platform right now.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionTimeoutRow(
    minutes: Int,
    onChange: (Int) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val options = listOf(5, 10, 15, 30, 60, 120)

    SettingRow(
        title = "Session timeout",
        subtitle = "Automatically logs users out after inactivity."
    ) {
        ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = !open }) {
            OutlinedTextField(
                value = "$minutes min",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier.menuAnchor().width(140.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) }
            )
            ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { m ->
                    DropdownMenuItem(
                        text = { Text("$m min") },
                        onClick = { onChange(m); open = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun MinPasswordLengthRow(
    value: Int,
    onChange: (Int) -> Unit
) {
    var slider by remember(value) { mutableStateOf(value.toFloat()) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Minimum password length", fontWeight = FontWeight.SemiBold)
            Text(value.toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        Slider(
            value = slider,
            onValueChange = {
                slider = it
                onChange(it.toInt().coerceIn(6, 20))
            },
            valueRange = 6f..20f
        )

        Text(
            "Recommended: 10+ characters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------- Data Store (UI-only now) ---------------- */

data class SystemSettingsUi(
    val mfaRequiredForPrivileged: Boolean = true,
    val sessionTimeoutMinutes: Int = 15,
    val maintenanceMode: Boolean = false,
    val minPasswordLength: Int = 10,
    val requireSpecialChar: Boolean = true,
    val auditLoggingEnabled: Boolean = true,
    val emailAlertsEnabled: Boolean = true
)

/**
 * UI-only settings store.
 * Later we will replace with Firestore:
 * /system/settings document.
 */
object SettingsStore {
    private var cache = SystemSettingsUi()

    fun load(): SystemSettingsUi = cache
    fun save(newValue: SystemSettingsUi) { cache = newValue }
}

@Preview
@Composable
private fun SystemSettingsScreenPreview() {
    SystemSettingsScreen(rememberNavController())
}