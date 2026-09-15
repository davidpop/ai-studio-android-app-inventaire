package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            val currentTheme by ThemeManager.currentTheme.collectAsState()
            MyApplicationTheme(themeMode = currentTheme) {
                AppNavigation()
            }
        }
    }
}
