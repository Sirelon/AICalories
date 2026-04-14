package com.sirelon.aicalories.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class AppColors(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val outline: Color,
    val warning: Color,
    val warningVariant: Color,
    val onSurfaceMuted: Color,
    val onSurfaceSoft: Color,
    val surfaceSubtle: Color,
    val infoSurface: Color,
    val infoSurfaceVariant: Color,
)

internal fun AppColors.toMaterial(darkTheme: Boolean): ColorScheme {
    val baseScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    return baseScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onSecondaryContainer,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
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
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        error = error,
        onError = onError,
        errorContainer = error.copy(alpha = 0.2f),
        onErrorContainer = error,
        outline = outline,
        outlineVariant = outlineVariant,
        inverseSurface = Color(0xFF2A1400),
        inverseOnSurface = Color(0xFFFFEDD8),
    )
}
