package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    secondary = NeonTeal,
    tertiary = Pink80,
    background = ObsidianBg,
    surface = ObsidianSurface,
    onBackground = PureWhite,
    onSurface = PureWhite,
    primaryContainer = ObsidianCard,
    onPrimaryContainer = PureWhite,
    secondaryContainer = BorderColor,
    onSecondaryContainer = NeonTeal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for obsidian experience
    dynamicColor: Boolean = false, // Disable dynamic dynamic coloring to retain custom premium look
    content: @Composable () -> Unit
) {
    // We enforce the customized obsidian dark theme as requested by the user
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
