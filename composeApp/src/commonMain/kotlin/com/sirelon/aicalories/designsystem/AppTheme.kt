package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun AiCaloriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: AppColors = if (darkTheme) ThemesFactory.dark() else ThemesFactory.light(),
    typography: AppTypography = appTypography(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial(darkTheme),
            typography = typography.toMaterialTypography(),
            content = content,
        )
    }
}

object AppTheme {

    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}

private val LocalAppColors = staticCompositionLocalOf { ThemesFactory.light() }
private val LocalAppTypography = staticCompositionLocalOf { appTypography() }
