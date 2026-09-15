package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ThemeGradients(
    val backgroundBrush: Brush,
    val surfaceGradientBrush: Brush,
    val cardSurfaceBrush: Brush,
    val cardBorderBrush: Brush,
    val heroBrush: Brush,
    val primaryButtonBrush: Brush,
    val accentBrush: Brush,
    val topBarBrush: Brush,
    val reliefGlowColor: Color,
    val chipBrush: Brush
)

val LocalThemeGradients = staticCompositionLocalOf<ThemeGradients> {
    error("No ThemeGradients provided")
}

val AppGradients: ThemeGradients
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeGradients.current

fun createThemeGradients(themeMode: AppThemeMode): ThemeGradients {
    return when (themeMode) {
        AppThemeMode.LIGHT -> ThemeGradients(
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE8EEF5),
                    Color(0xFFF1F5F9),
                    Color(0xFFF8FAFC)
                )
            ),
            surfaceGradientBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC)
                )
            ),
            cardSurfaceBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF6F8FB)
                )
            ),
            cardBorderBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE2E8F0),
                    Color(0x66CBD5E1)
                )
            ),
            heroBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF1E40AF),
                    Color(0xFF2563EB),
                    Color(0xFF0284C7)
                )
            ),
            primaryButtonBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF2563EB),
                    Color(0xFF1D4ED8)
                )
            ),
            accentBrush = Brush.linearGradient(
                listOf(
                    Color(0xFFD97706),
                    Color(0xFFF59E0B)
                )
            ),
            topBarBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF1F5F9)
                )
            ),
            reliefGlowColor = Color(0x182563EB),
            chipBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFEFF6FF),
                    Color(0xFFDBEAFE)
                )
            )
        )

        AppThemeMode.DARK -> ThemeGradients(
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0B101D),
                    Color(0xFF0F172A),
                    Color(0xFF131D33)
                )
            ),
            surfaceGradientBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF1E293B),
                    Color(0xFF172033)
                )
            ),
            cardSurfaceBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF27354A),
                    Color(0xFF1E293B),
                    Color(0xFF192231)
                )
            ),
            cardBorderBrush = Brush.verticalGradient(
                listOf(
                    Color(0x6693C5FD),
                    Color(0x3360A5FA),
                    Color(0x103B82F6)
                )
            ),
            heroBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF1E3A8A),
                    Color(0xFF1E293B),
                    Color(0xFF0F172A)
                )
            ),
            primaryButtonBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF3B82F6),
                    Color(0xFF2563EB)
                )
            ),
            accentBrush = Brush.linearGradient(
                listOf(
                    Color(0xFFFBBF24),
                    Color(0xFFF59E0B)
                )
            ),
            topBarBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF1E293B),
                    Color(0xFF131D33)
                )
            ),
            reliefGlowColor = Color(0x3360A5FA),
            chipBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF1E3A8A),
                    Color(0xFF172554)
                )
            )
        )

        AppThemeMode.EMERALD -> ThemeGradients(
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE2F5E9),
                    Color(0xFFECF9F1),
                    Color(0xFFF2FDF6)
                )
            ),
            surfaceGradientBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF0FDF4)
                )
            ),
            cardSurfaceBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF4FCF7)
                )
            ),
            cardBorderBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFD1FAE5),
                    Color(0x88A7F3D0)
                )
            ),
            heroBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF064E3B),
                    Color(0xFF047857),
                    Color(0xFF059669)
                )
            ),
            primaryButtonBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF059669),
                    Color(0xFF047857)
                )
            ),
            accentBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF0D9488),
                    Color(0xFF14B8A6)
                )
            ),
            topBarBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE6F7ED)
                )
            ),
            reliefGlowColor = Color(0x22059669),
            chipBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFDCFCE7),
                    Color(0xFFD1FAE5)
                )
            )
        )

        AppThemeMode.TERRACOTTA -> ThemeGradients(
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFDE8D0),
                    Color(0xFFFEF0DF),
                    Color(0xFFFFFBEB)
                )
            ),
            surfaceGradientBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFF7ED)
                )
            ),
            cardSurfaceBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFF5EA)
                )
            ),
            cardBorderBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFEDD5),
                    Color(0x88FED7AA)
                )
            ),
            heroBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF7C2D12),
                    Color(0xFFC2410C),
                    Color(0xFFEA580C)
                )
            ),
            primaryButtonBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFEA580C),
                    Color(0xFFC2410C)
                )
            ),
            accentBrush = Brush.linearGradient(
                listOf(
                    Color(0xFFD97706),
                    Color(0xFFF59E0B)
                )
            ),
            topBarBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFEEAD2)
                )
            ),
            reliefGlowColor = Color(0x25EA580C),
            chipBrush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFFFEDD5),
                    Color(0xFFFED7AA)
                )
            )
        )
    }
}

/**
 * Modifier extension that wraps an element in a relief card appearance
 * with subtle top-lit surface gradient, directional bevel border, and soft elevation shadow.
 */
fun Modifier.themeCardRelief(
    shape: Shape,
    borderBrush: Brush? = null,
    surfaceBrush: Brush? = null,
    elevation: Dp = 2.dp,
    borderWidth: Dp = 1.dp
): Modifier {
    var mod = this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false
    ).clip(shape)

    if (surfaceBrush != null) {
        mod = mod.background(surfaceBrush)
    }

    if (borderBrush != null) {
        mod = mod.border(
            width = borderWidth,
            brush = borderBrush,
            shape = shape
        )
    }
    return mod
}
