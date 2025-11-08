package com.sirelon.aicalories.designsystem

import androidx.compose.ui.graphics.Color

object ThemesFactory {

    fun light(): AppColors = AppColors(
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        background = Color(0xFFF6F6F9),
        onBackground = Color(0xFF1F2933),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1F2933),
        surfaceVariant = Color(0xFF1F2933).copy(alpha = 0.08f),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF1B8E5A),
        outline = Color(0xFFCBD5E1),
    )

    fun dark(): AppColors = AppColors(
        primary = Color(0xFFB4C6FF),
        onPrimary = Color(0xFF121530),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE5E7EB),
        surface = Color(0xFF1C1C1E),
        onSurface = Color(0xFFE5E7EB),
        surfaceVariant = Color(0xFFE5E7EB).copy(alpha = 0.08f),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF680003),
        success = Color(0xFF4FD28A),
        outline = Color(0xFF3F4753),
    )


}
