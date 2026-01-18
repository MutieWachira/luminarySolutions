package com.example.luminarysolutions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.luminarysolutions.ui.navigation.AppNavHost
import com.example.luminarysolutions.ui.theme.LuminarySolutionsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()//Allows content behind system bars recommended for immersive UI

        setContent {//Entry point for compose and defines the compose UI tree
            LuminarySolutionsTheme {
                AppNavHost(
                    modifier = Modifier.fillMaxSize()
                )//hosts all screens and controls navigation path
            }//Themes provide centralized UI styling.
        }
    }
}
