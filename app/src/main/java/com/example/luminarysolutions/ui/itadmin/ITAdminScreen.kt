package com.example.luminarysolutions.ui.itadmin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ITAdminScreen(viewModel: ITAdminViewModel = viewModel()) {

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "IT Admin - User Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(viewModel.users) { user ->
                UserRow(user)
            }
        }

    }
}

@Preview
@Composable
fun ITAdminScreenPreview() {
    ITAdminScreen()
}


