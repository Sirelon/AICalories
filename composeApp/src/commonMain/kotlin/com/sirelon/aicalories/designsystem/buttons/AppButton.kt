package com.sirelon.aicalories.designsystem.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonDefaults.primary(),
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(AppDimens.BorderRadius.xl4)
    val gradient = style.gradient

    val backgroundModifier = if (gradient != null && enabled) {
        modifier
            .height(AppDimens.Size.xl8 + AppDimens.Size.xl7)
            .background(gradient, shape)
    } else {
        modifier.height(AppDimens.Size.xl8 + AppDimens.Size.xl7)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = backgroundModifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (gradient != null) Color.Transparent else style.backgroundColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.backgroundColor.copy(alpha = 0.55f),
            disabledContentColor = style.contentColor.copy(alpha = 0.7f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = style.elevation),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            leadingIcon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.Size.xl5),
                )
            }
            Text(
                text = text,
                fontSize = AppDimens.TextSize.xl3,
                fontWeight = FontWeight.Bold,
            )
            trailingIcon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.Size.xl5),
                )
            }
        }
    }
}

data class AppButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val elevation: Dp = AppDimens.Size.xs,
    val gradient: Brush? = null,
)

data object AppButtonDefaults {

    @Composable
    @ReadOnlyComposable
    fun primary(): AppButtonStyle {
        val primary = AppTheme.colors.primary
        val primaryBright = AppTheme.colors.primaryBright
        return AppButtonStyle(
            backgroundColor = primary,
            contentColor = AppTheme.colors.onPrimary,
            gradient = Brush.linearGradient(
                colors = listOf(primary, primaryBright),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            ),
        )
    }

    @Composable
    @ReadOnlyComposable
    fun secondary(): AppButtonStyle {
        return AppButtonStyle(
            backgroundColor = AppTheme.colors.surfaceHigh,
            contentColor = AppTheme.colors.onSurface,
            elevation = AppDimens.Spacing.xs4,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun outline(): AppButtonStyle {
        return AppButtonStyle(
            backgroundColor = AppTheme.colors.surfaceHigh,
            contentColor = AppTheme.colors.onBackground,
            elevation = AppDimens.Spacing.xs4,
        )
    }
}
