package com.sirelon.aicalories.designsystem.buttons

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(AppDimens.Size.xl8 + AppDimens.Size.xl7),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl4),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor,
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
                    imageVector = it,
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
                    imageVector = it,
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
)

data object AppButtonDefaults {


    @Composable
    @ReadOnlyComposable
    fun primary(): AppButtonStyle {
        return AppButtonStyle(
            backgroundColor = AppTheme.colors.primary,
            contentColor = AppTheme.colors.onPrimary,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun secondary(): AppButtonStyle {
        return AppButtonStyle(
            backgroundColor = AppTheme.colors.warning,
            contentColor = AppTheme.colors.onPrimary,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun outline(): AppButtonStyle {
        return AppButtonStyle(
            backgroundColor = AppTheme.colors.outline,
            contentColor = AppTheme.colors.onBackground,
            elevation = AppDimens.Spacing.xs4,
        )
    }
}
