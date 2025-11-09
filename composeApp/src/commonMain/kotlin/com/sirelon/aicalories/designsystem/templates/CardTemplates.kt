package com.sirelon.aicalories.designsystem.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppTheme

@Composable
fun CardWithTitle(
    title: String,
    spacing: Dp = AppDimens.Spacing.xl,
    content: @Composable ColumnScope.() -> Unit,
) {
    CardWithTitle(
        title = {
            Text(text = title)
        },
        content = content,
        spacing = spacing,
    )
}

@Composable
fun CardWithTitle(
    spacing: Dp = AppDimens.Spacing.xl,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            CompositionLocalProvider(LocalTextStyle provides AppTheme.typography.title) {
                title()
            }

            AppDivider()

            content()
        }
    }
}