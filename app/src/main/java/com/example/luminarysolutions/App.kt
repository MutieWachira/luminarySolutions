package com.example.luminarysolutions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.luminarysolutions.ui.navigation.AppNavHost
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme

@Composable
fun App() {
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
