package com.sirelon.aicalories.features.agile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.sirelon.aicalories.designsystem.AppTheme

enum class Estimation {
    XS,
    S,
    M,
    L,
    XL,
}

fun Estimation.code(): String = when (this) {
    Estimation.XS -> "XS"
    Estimation.S -> "S"
    Estimation.M -> "M"
    Estimation.L -> "L"
    Estimation.XL -> "XL"
}

fun Estimation.description(): String = when (this) {
    Estimation.XS -> "Extra Small"
    Estimation.S -> "Small"
    Estimation.M -> "Medium"
    Estimation.L -> "Large"
    Estimation.XL -> "Extra Large"
}

@Composable
fun Estimation.color(): Color = when (this) {
    Estimation.XS -> AppTheme.colors.success
    Estimation.S -> AppTheme.colors.primary
    Estimation.M -> AppTheme.colors.warning
    Estimation.L -> AppTheme.colors.warningVariant
    Estimation.XL -> AppTheme.colors.error
}

@Composable
fun Estimation.icon(): Painter = when (this) {
    Estimation.XS -> rememberVectorPainter(Icons.Outlined.RadioButtonUnchecked)
    Estimation.S -> rememberVectorPainter(Icons.Outlined.Circle)
    Estimation.M -> rememberVectorPainter(Icons.Outlined.StopCircle)
    Estimation.L -> rememberVectorPainter(Icons.Outlined.CropSquare)
    Estimation.XL -> rememberVectorPainter(Icons.Outlined.AspectRatio)
}
