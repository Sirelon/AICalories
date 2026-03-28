package com.sirelon.aicalories.designsystem.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme

@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.surfaceSubtle,
    contentColor: Color = AppTheme.colors.onSurfaceMuted,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(AppDimens.Size.xl8 + AppDimens.Size.xl7),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl4),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.Size.xl6),
        )
    }
}
