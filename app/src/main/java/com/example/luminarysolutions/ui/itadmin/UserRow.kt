package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UserRow(user: UserItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = user.email)
            Text(text = "Role: ${user.role}")
            Text(
                text = if (user.enabled) "Active" else "Disabled",
                color = if (user.enabled) Color.Green else Color.Red
            )
        }
    }
}