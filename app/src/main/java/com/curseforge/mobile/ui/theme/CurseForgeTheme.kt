package com.curseforge.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CurseForgeDarkScheme = darkColorScheme(
    primary = Color(0xFFF16436),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFF8A65),
    onSecondary = Color(0xFF1A1A1A),
    background = Color(0xFF121212),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFD0D0D0),
    error = Color(0xFFFF6B6B)
)

@Composable
fun CurseForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CurseForgeDarkScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
