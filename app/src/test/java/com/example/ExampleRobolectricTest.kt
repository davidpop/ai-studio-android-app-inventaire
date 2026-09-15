package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Inventaire", appName)
  }

  @Test
  fun `test theme manager persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.ui.theme.ThemeManager.init(context)
    assertEquals(com.example.ui.theme.AppThemeMode.LIGHT, com.example.ui.theme.ThemeManager.currentTheme.value)

    com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.DARK)
    assertEquals(com.example.ui.theme.AppThemeMode.DARK, com.example.ui.theme.ThemeManager.currentTheme.value)

    com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.EMERALD)
    assertEquals(com.example.ui.theme.AppThemeMode.EMERALD, com.example.ui.theme.ThemeManager.currentTheme.value)

    com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.TERRACOTTA)
    assertEquals(com.example.ui.theme.AppThemeMode.TERRACOTTA, com.example.ui.theme.ThemeManager.currentTheme.value)

    // Re-initialize from context to ensure persistence in SharedPreferences works
    com.example.ui.theme.ThemeManager.init(context)
    assertEquals(com.example.ui.theme.AppThemeMode.TERRACOTTA, com.example.ui.theme.ThemeManager.currentTheme.value)
  }
}
