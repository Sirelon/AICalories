package com.sirelon.aicalories.designsystem

import androidx.compose.ui.graphics.Color

object ThemesFactory {

    fun light(): AppColors = AppColors(
        primary = LightTokens.Primary,
        onPrimary = LightTokens.OnPrimary,
        background = LightTokens.Background,
        onBackground = LightTokens.OnBackground,
        surface = LightTokens.Surface,
        onSurface = LightTokens.OnSurface,
        error = LightTokens.Error,
        onError = LightTokens.OnError,
        success = LightTokens.Success,
        outline = LightTokens.Outline,
    )

    fun dark(): AppColors = AppColors(
        primary = DarkTokens.Primary,
        onPrimary = DarkTokens.OnPrimary,
        background = DarkTokens.Background,
        onBackground = DarkTokens.OnBackground,
        surface = DarkTokens.Surface,
        onSurface = DarkTokens.OnSurface,
        error = DarkTokens.Error,
        onError = DarkTokens.OnError,
        success = DarkTokens.Success,
        outline = DarkTokens.Outline,
    )

    private object LightTokens {
        val Primary = Color(0xFF4F46E5)
        val OnPrimary = Color(0xFFFFFFFF)
        val Background = Color(0xFFF6F6F9)
        val OnBackground = Color(0xFF1F2933)
        val Surface = Color(0xFFFFFFFF)
        val OnSurface = Color(0xFF1F2933)
        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)
        val Success = Color(0xFF1B8E5A)
        val Outline = Color(0xFFCBD5E1)
    }

    private object DarkTokens {
        val Primary = Color(0xFFB4C6FF)
        val OnPrimary = Color(0xFF121530)
        val Background = Color(0xFF121212)
        val OnBackground = Color(0xFFE5E7EB)
        val Surface = Color(0xFF1C1C1E)
        val OnSurface = Color(0xFFE5E7EB)
        val Error = Color(0xFFFFB4AB)
        val OnError = Color(0xFF680003)
        val Success = Color(0xFF4FD28A)
        val Outline = Color(0xFF3F4753)
    }
}
