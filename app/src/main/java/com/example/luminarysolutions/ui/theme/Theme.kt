package com.example.luminarysolutions.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme
val FundGreen = Color(0xFF00B964)
val TrustBlue = Color(0xFF1E88E5)
val SoftAccent = Color(0xFFFFC107)

// Dark theme
val FundGreenDark = Color(0xFF66E2A3)
val TrustBlueDark = Color(0xFF90CAF9)
val SoftAccentDark = Color(0xFFFFD54F)
private val LightColors = lightColorScheme(
    primary = FundGreen,
    secondary = TrustBlue,
    tertiary = SoftAccent
)

private val DarkColors = darkColorScheme(
    primary = FundGreenDark,
    secondary = TrustBlueDark,
    tertiary = SoftAccentDark
)

@Composable
fun LuminarySolutionsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}