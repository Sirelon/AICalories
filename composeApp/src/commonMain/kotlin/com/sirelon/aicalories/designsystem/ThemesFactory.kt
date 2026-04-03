package com.sirelon.aicalories.designsystem

import androidx.compose.ui.graphics.Color

object ThemesFactory {

    fun light(): AppColors = AppColors(
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1F2933),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1F2933),
        surfaceVariant = Color(0xFF1F2933).copy(alpha = 0.08f),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF1B8E5A),
        outline = Color(0xFFD8DADB),
        warning = Color(0xFFE67E22),
        warningVariant = Color(0xFFF28B44),
        onSurfaceMuted = Color(0xFF94A3B8),
        onSurfaceSoft = Color(0xFF64748B),
        surfaceSubtle = Color(0xFFF1F5F9),
        infoSurface = Color(0xFFF0F4FF),
        infoSurfaceVariant = Color(0xFFDCE4FF),
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
        warning = Color(0xFFF59E0B),
        warningVariant = Color(0xFFFBBF24),
        onSurfaceMuted = Color(0xFF94A3B8),
        onSurfaceSoft = Color(0xFFA8B6C7),
        surfaceSubtle = Color(0xFF28313C),
        infoSurface = Color(0xFF232B45),
        infoSurfaceVariant = Color(0xFF33406A),
    )


}
