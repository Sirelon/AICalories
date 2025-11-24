package com.sirelon.aicalories.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme

@Composable
fun AppNavigationLayout(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val isWide = windowInfo.containerDpSize.width >= 720.dp
    val gradient = Brush.verticalGradient(
        listOf(
            AppTheme.colors.background,
            AppTheme.colors.surface,
        ),
    )
    val outline = AppTheme.colors.outline.copy(alpha = 0.15f)
    val containerColor = Color.Transparent

    if (isWide) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight(),
                color = containerColor,
                tonalElevation = 0.dp,
            ) {
                NavigationRail(
                    modifier = Modifier
                        .widthIn(min = 104.dp)
                        .padding(vertical = AppDimens.Spacing.xl3),
                    header = {
                        Text(
                            modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                            text = "AI Calories",
                            style = AppTheme.typography.headline,
                            color = AppTheme.colors.onSurface,
                        )
                    },
                ) {
                    topLevelDestinations.forEach { item ->
                        val isSelected = currentDestination == item.destination
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { onNavigate(item.destination) },
                            icon = {
                                item.Icon(
                                    tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurface.copy(
                                        alpha = 0.75f,
                                    ),
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = outline,
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = AppDimens.Spacing.xl4)
                    .fillMaxSize(),
            ) {
                content()
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = containerColor,
        ) {
            androidx.compose.material3.Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient),
                containerColor = containerColor,
                bottomBar = {
                    NavigationBar(
                        containerColor = AppTheme.colors.surface.copy(alpha = 0.95f),
                    ) {
                        topLevelDestinations.forEach { item ->
                            val isSelected = currentDestination == item.destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { onNavigate(item.destination) },
                                icon = {
                                    item.Icon(
                                        tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurface.copy(
                                            alpha = 0.8f,
                                        ),
                                    )
                                },
                                label = { Text(item.label) },
                            )
                        }
                    }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    content()
                }
            }
        }
    }
}

private val topLevelDestinations = listOf(
    AppNavItem(AppDestination.Analyze, "Analyze", Icons.Filled.Search),
    AppNavItem(AppDestination.History, "History", Icons.Filled.History),
    AppNavItem(AppDestination.Agile, "Agile", Icons.Filled.Dashboard),
)

private data class AppNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
) {
    @Composable
    fun Icon(tint: Color) {
        androidx.compose.material3.Icon(imageVector = icon, contentDescription = label, tint = tint)
    }
}
