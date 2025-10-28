package com.sirelon.aicalories.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Stable
data class AppTypography(
    val headline: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
)

fun appTypography(
    fontFamily: FontFamily = FontFamily.SansSerif
): AppTypography {
    val headline = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    )
    val title = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    )
    val body = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )
    val label = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )
    val caption = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    )

    return AppTypography(
        headline = headline,
        title = title,
        body = body,
        label = label,
        caption = caption,
    )
}

internal fun AppTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = headline,
    displayMedium = headline,
    displaySmall = headline,
    headlineLarge = headline,
    headlineMedium = title.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = title,
    titleLarge = title,
    titleMedium = body.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = body,
    bodySmall = caption,
    labelLarge = label,
    labelMedium = label.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = caption,
)
