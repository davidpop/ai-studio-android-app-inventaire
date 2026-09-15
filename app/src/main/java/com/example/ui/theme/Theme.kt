package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary
)

private val EmeraldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldOnSecondaryContainer,
    tertiary = EmeraldTertiary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    surfaceVariant = EmeraldSurfaceVariant,
    onBackground = EmeraldTextPrimary,
    onSurface = EmeraldTextPrimary,
    onSurfaceVariant = EmeraldTextSecondary
)

private val TerracottaColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaPrimaryContainer,
    onPrimaryContainer = TerracottaOnPrimaryContainer,
    secondary = TerracottaSecondary,
    onSecondary = Color.White,
    secondaryContainer = TerracottaSecondaryContainer,
    onSecondaryContainer = TerracottaOnSecondaryContainer,
    tertiary = TerracottaTertiary,
    background = TerracottaBackground,
    surface = TerracottaSurface,
    surfaceVariant = TerracottaSurfaceVariant,
    onBackground = TerracottaTextPrimary,
    onSurface = TerracottaTextPrimary,
    onSurfaceVariant = TerracottaTextSecondary
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.EMERALD -> EmeraldColorScheme
        AppThemeMode.TERRACOTTA -> TerracottaColorScheme
    }

    val gradients = createThemeGradients(themeMode)

    CompositionLocalProvider(LocalThemeGradients provides gradients) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

