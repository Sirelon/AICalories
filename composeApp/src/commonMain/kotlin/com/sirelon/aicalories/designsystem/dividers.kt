package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.outline.copy(alpha = 0.15f),
    )
}