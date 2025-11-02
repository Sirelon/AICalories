package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Cell(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    overline: @Composable (() -> Unit)? = null,
    supporting: @Composable (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = onClick) } ?: Modifier
    ListItem(
        headlineContent = headline,
        modifier = modifier.then(clickableModifier),
        overlineContent = overline,
        supportingContent = supporting,
        leadingContent = leading,
        trailingContent = trailing,
        colors = ListItemDefaults.colors(),
        tonalElevation = ListItemDefaults.Elevation,
        shadowElevation = ListItemDefaults.Elevation,
    )
}
