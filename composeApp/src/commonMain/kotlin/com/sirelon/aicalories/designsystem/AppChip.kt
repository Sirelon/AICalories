package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

@Composable
fun AppChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    colors: AppChipColors = AppChipDefaults.neutralColors(),
) {
    val resolvedLeadingIcon = leadingIcon ?: icon?.let { image ->
        {
            Icon(
                imageVector = image,
                contentDescription = iconContentDescription,
            )
        }
    }

    val borderStroke = colors.borderColor?.takeIf { it.alpha > 0f }?.let {
        BorderStroke(AppDimens.BorderWidth.xs, it)
    }

    AssistChip(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.BorderRadius.l),
        label = {
            Text(
                text = text,
                style = AppTheme.typography.caption,
                color = colors.labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = AppDimens.Spacing.xs3),
            )
        },
        leadingIcon = resolvedLeadingIcon?.let { iconContent ->
            {
                CompositionLocalProvider(LocalContentColor provides colors.leadingIconColor) {
                    iconContent()
                }
            }
        },
        colors = colors.toChipColors(),
        border = borderStroke,
        elevation = AssistChipDefaults.assistChipElevation(defaultElevation = 1.dp),
    )
}

@Immutable
data class AppChipColors(
    val containerColor: Color,
    val labelColor: Color,
    val leadingIconColor: Color,
    val borderColor: Color? = null,
)

object AppChipDefaults {

    @Composable
    @ReadOnlyComposable
    fun neutralColors(): AppChipColors {
        val scheme = MaterialTheme.colorScheme
        return AppChipColors(
            containerColor = scheme.surfaceVariant.copy(alpha = 0.9f),
            labelColor = scheme.onSurfaceVariant,
            leadingIconColor = scheme.primary,
            borderColor = scheme.outline.copy(alpha = 0.4f),
        )
    }

    @Composable
    @ReadOnlyComposable
    fun primaryColors(): AppChipColors {
        return accentColors(
            accent = AppTheme.colors.primary,
            onAccent = AppTheme.colors.primary,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun successColors(): AppChipColors {
        return accentColors(
            accent = AppTheme.colors.success,
            onAccent = AppTheme.colors.success,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun errorColors(): AppChipColors {
        return accentColors(
            accent = AppTheme.colors.error,
            onAccent = AppTheme.colors.error,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun accentColors(
        accent: Color,
        onAccent: Color = accent,
    ): AppChipColors {
        return AppChipColors(
            containerColor = accent.copy(alpha = 0.12f),
            labelColor = onAccent,
            leadingIconColor = onAccent,
            borderColor = accent.copy(alpha = 0.45f),
        )
    }
}

@Composable
private fun AppChipColors.toChipColors() = AssistChipDefaults.assistChipColors(
    containerColor = containerColor,
    labelColor = labelColor,
    leadingIconContentColor = leadingIconColor,
    trailingIconContentColor = leadingIconColor,
    disabledContainerColor = containerColor,
    disabledLabelColor = labelColor,
    disabledLeadingIconContentColor = leadingIconColor,
    disabledTrailingIconContentColor = leadingIconColor,
)
