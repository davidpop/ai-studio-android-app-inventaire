package com.example.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val primaryPreviewColor: Color,
    val secondaryPreviewColor: Color,
    val backgroundPreviewColor: Color
) {
    LIGHT(
        id = "light",
        displayName = "Mode Jour",
        subtitle = "Clair, épuré & lumineux",
        primaryPreviewColor = Color(0xFF2563EB),
        secondaryPreviewColor = Color(0xFFF59E0B),
        backgroundPreviewColor = Color(0xFFF8FAFC)
    ),
    DARK(
        id = "dark",
        displayName = "Mode Sombre",
        subtitle = "Élégant, ardoise & reposant",
        primaryPreviewColor = Color(0xFF93C5FD),
        secondaryPreviewColor = Color(0xFFFCD34D),
        backgroundPreviewColor = Color(0xFF0F172A)
    ),
    EMERALD(
        id = "emerald",
        displayName = "Thème Émeraude",
        subtitle = "Nature végétale & sauge fraîche",
        primaryPreviewColor = Color(0xFF059669),
        secondaryPreviewColor = Color(0xFF0D9488),
        backgroundPreviewColor = Color(0xFFF0FDF4)
    ),
    TERRACOTTA(
        id = "terracotta",
        displayName = "Thème Terracotta",
        subtitle = "Ambre chaud & terre cuite",
        primaryPreviewColor = Color(0xFFEA580C),
        secondaryPreviewColor = Color(0xFFD97706),
        backgroundPreviewColor = Color(0xFFFFFBEB)
    )
}

object ThemeManager {
    private const val PREFS_NAME = "inventory_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    private val _currentTheme = MutableStateFlow(AppThemeMode.LIGHT)
    val currentTheme: StateFlow<AppThemeMode> = _currentTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_THEME_ID, AppThemeMode.LIGHT.id)
        val theme = AppThemeMode.values().find { it.id == savedId } ?: AppThemeMode.LIGHT
        _currentTheme.value = theme
    }

    fun setTheme(context: Context, mode: AppThemeMode) {
        _currentTheme.value = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, mode.id)
            .apply()
    }
}
