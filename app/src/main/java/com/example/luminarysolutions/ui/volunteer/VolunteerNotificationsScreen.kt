package com.example.luminarysolutions.ui.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.luminarysolutions.data.models.Notification
import com.example.luminarysolutions.ui.volunteer.viewmodel.VolunteerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerNotificationsScreen(vm: VolunteerViewModel) {
    val notifications by vm.notifications.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notifications") }) }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications yet", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(notifications) { notification ->
                    NotificationItem(notification) {
                        vm.markAsRead(notification.id)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val dateFormatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    ListItem(
        headlineContent = { Text(notification.title, fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal) },
        supportingContent = { 
            Column {
                Text(notification.message)
                Text(dateFormatter.format(Date(notification.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        leadingContent = {
            val icon = when (notification.type) {
                "ACHIEVEMENT" -> Icons.Default.Celebration
                "INFO" -> Icons.Default.Info
                else -> Icons.Default.Notifications
            }
            val color = when (notification.type) {
                "ACHIEVEMENT" -> Color(0xFFFFD700)
                "SUCCESS" -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
        },
        trailingContent = {
            if (!notification.isRead) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
