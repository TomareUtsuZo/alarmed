package com.alarmed.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6750A4),
    secondary = androidx.compose.ui.graphics.Color(0xFF625B71),
    tertiary = androidx.compose.ui.graphics.Color(0xFF7D5260)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    secondary = androidx.compose.ui.graphics.Color(0xFFCCC2DC),
    tertiary = androidx.compose.ui.graphics.Color(0xFFEFB8C8)
)

/**
 * Main theme for the Alarmed app.
 * Uses Material 3 design system as specified in the Development Plan.
 */
@Composable
fun AlarmedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
