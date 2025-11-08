package com.sirelon.aicalories.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val outline: Color,
)

internal fun AppColors.toMaterial(darkTheme: Boolean): ColorScheme {
    val baseScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    return baseScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = surface,
        onPrimaryContainer = onSurface,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = surface,
        onSecondaryContainer = onSurface,
        tertiary = success,
        onTertiary = onPrimary,
        tertiaryContainer = success.copy(alpha = 0.25f),
        onTertiaryContainer = success,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurface,
        error = error,
        onError = onError,
        errorContainer = error.copy(alpha = 0.2f),
        onErrorContainer = error,
        outline = outline,
        outlineVariant = outline.copy(alpha = 0.6f),
    )
}
