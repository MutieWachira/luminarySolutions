package com.example.luminarysolutions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.luminarysolutions.data.firebase.FirestoreService
import com.example.luminarysolutions.ui.navigation.AppNavHost
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme

@Composable
fun App() {
    // Seed initial achievements for the Volunteer Module
    LaunchedEffect(Unit) {
        FirestoreService.seedAchievements()
    }

    //Apply global theme
    LuminarySolutionsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            //navigation starts here
            AppNavHost()
        }
    }
}
