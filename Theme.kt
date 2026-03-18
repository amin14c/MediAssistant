package com.example.mediassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    errorContainer = Color(0xFFFFEBEE)
)

@Composable
fun MediAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
