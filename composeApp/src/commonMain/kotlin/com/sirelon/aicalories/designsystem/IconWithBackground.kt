package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun IconWithBackground(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppDimens.BorderRadius.xl),
    iconPadding: Dp = AppDimens.Spacing.l,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
    ) {
        Box(
            modifier = Modifier.padding(iconPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
