package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.outline.copy(alpha = 0.5f),
    )
}

@Composable
fun AppDivider(middleContent: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppDivider(modifier = Modifier.weight(1f))
        middleContent()
        AppDivider(modifier = Modifier.weight(1f))
    }
}