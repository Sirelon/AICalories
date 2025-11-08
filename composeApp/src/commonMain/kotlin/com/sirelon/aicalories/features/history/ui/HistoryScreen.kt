@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.sirelon.aicalories.features.history.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ChipComponent
import com.sirelon.aicalories.designsystem.ChipStyle
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.TagGroup
import com.sirelon.aicalories.designsystem.screens.EmptyScreen
import com.sirelon.aicalories.features.history.presentation.HistorySampleDataProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    renderModel: HistoryScreenRenderModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onEntryClick: (HistoryEntryRenderModel) -> Unit = {},
    onEmptyStateAction: (() -> Unit)? = null,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "History & Insights",
                subtitle = "Track your analysed meals",
                onBack = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if (renderModel.isEmpty) {
            EmptyScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                title = if (isLoading) "Loading history" else "No history yet",
                description = if (isLoading) "Wait a moment" else "Your analysed meals will appear here once you capture and submit them.",
                actionLabel = "Capture meal".takeUnless { isLoading },
                onActionClick = onEmptyStateAction.takeUnless { isLoading },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = AppDimens.Spacing.xl6,
                    end = AppDimens.Spacing.xl6,
                    bottom = AppDimens.Spacing.xl8,
                    top = AppDimens.Spacing.xl5,
                ),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl5),
            ) {
                item(key = "history_header") {
                    TagGroup(
                        title = "Insights",
                        tags = renderModel.insights,
                        style = ChipStyle.Success
                    )
                }

                renderModel.weeklySummary?.let { weekly ->
                    item(key = "weekly_summary") {
                        WeeklyCaloriesCard(weekly)
                    }
                }

                items(
                    items = renderModel.groupedEntries,
                    key = { it.groupId },
                ) { group ->
                    HistoryGroupSection(
                        group = group,
                        highlightedEntryId = renderModel.highlightedEntryId,
                        onEntryClick = onEntryClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyCaloriesCard(
    model: WeeklyCaloriesRenderModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl4),
        color = AppTheme.colors.surface,
        tonalElevation = AppDimens.Size.xs,
        shadowElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl6),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = model.title,
                        style = AppTheme.typography.title,
                        color = AppTheme.colors.onSurface,
                    )
                    Text(
                        text = model.totalLabel,
                        style = AppTheme.typography.headline,
                        color = AppTheme.colors.onSurface,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    model.changeLabel?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.success,
                        )
                    }
                    model.targetLabel?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            WeeklyChart(points = model.points)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                model.points.forEach { point ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = point.dayLabel,
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                        Text(
                            text = point.caloriesLabel,
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyChart(points: List<CaloriePointRenderModel>) {
    if (points.isEmpty()) return
    val chartPoints = points
    val maxCalories = chartPoints.maxOf { it.caloriesValue }.coerceAtLeast(1)
    val primary = AppTheme.colors.primary
    val grid = AppTheme.colors.outline.copy(alpha = 0.2f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.Size.xl18),
    ) {
        val height = size.height
        val width = size.width
        val stepCount = 4
        repeat(stepCount + 1) { index ->
            val y = height * index / stepCount
            drawLine(
                color = grid,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = AppDimens.BorderWidth.xs.toPx(),
            )
        }

        val path = Path()
        chartPoints.forEachIndexed { index, point ->
            val ratio = point.caloriesValue.toFloat() / maxCalories
            val x =
                if (chartPoints.size == 1) width / 2f else width * index / (chartPoints.size - 1)
            val y = height - (ratio * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primary,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = AppDimens.BorderWidth.m.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        chartPoints.forEachIndexed { index, point ->
            val ratio = point.caloriesValue.toFloat() / maxCalories
            val x =
                if (chartPoints.size == 1) width / 2f else width * index / (chartPoints.size - 1)
            val y = height - (ratio * height)
            drawCircle(
                color = primary,
                radius = AppDimens.Size.m.toPx(),
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun HistoryGroupSection(
    group: HistoryGroupRenderModel,
    highlightedEntryId: Long?,
    onEntryClick: (HistoryEntryRenderModel) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = group.dayLabel,
            style = AppTheme.typography.title,
            color = AppTheme.colors.onSurface,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            group.entries.forEach { entry ->
                HistoryEntryCard(
                    entry = entry,
                    isHighlighted = entry.id == highlightedEntryId,
                    onClick = { onEntryClick(entry) },
                )
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: HistoryEntryRenderModel,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    val borderColor =
        if (isHighlighted) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.3f)
    val containerColor = if (isHighlighted) {
        AppTheme.colors.primary.copy(alpha = 0.08f)
    } else {
        AppTheme.colors.surface
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl4),
        color = containerColor,
        border = BorderStroke(AppDimens.BorderWidth.xs, borderColor),
        tonalElevation = AppDimens.Size.xs,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
                ) {
                    Text(
                        text = entry.dateLabel,
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.Size.xl - AppDimens.Size.xs),
                            tint = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(modifier = Modifier.width(AppDimens.Spacing.xs))
                        Text(
                            text = entry.timeLabel,
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.onSurface,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
                ) {
                    entry.caloriesLabel?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.title,
                            color = AppTheme.colors.onSurface,
                        )
                    }
                    entry.confidenceLabel?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                    }

                    ChipComponent(
                        data = entry.summary.qualityLabel,
                        style = ChipStyle.Neutral,
                    )
                }
            }

            entry.note?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.onSurface,
                )
            }

            if (entry.foods.isNotEmpty()) {
                FoodGrid(foods = entry.foods)
            }

            entry.macros?.let {
                MacroBreakdownRow(it)
            }

            entry.summary?.let {
                HistorySummarySection(it)
            }

            if (entry.attachments.isNotEmpty()) {
                AttachmentsRow(
                    attachments = entry.attachments,
                    photoCountLabel = entry.photoCountLabel,
                )
            }

            TagGroup(
                title = "Tags",
                tags = entry.tags,
                style = ChipStyle.Neutral,
            )
        }
    }
}

@Composable
private fun MacroBreakdownRow(macros: MacroBreakdownRenderModel) {
    Surface(
        color = AppTheme.colors.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl3),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MacroItem(label = "Calories", value = macros.calories)
            MacroItem(label = "Protein", value = macros.protein)
            MacroItem(label = "Fat", value = macros.fat)
            MacroItem(label = "Carbs", value = macros.carbs)
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = AppTheme.typography.label,
            color = AppTheme.colors.onSurface,
        )
    }
}

@Composable
private fun HistorySummarySection(summary: HistoryReportSummaryRenderModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        modifier = Modifier.fillMaxWidth(),
    ) {
        summary.advice?.let {
            Text(
                text = it,
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurface,
            )
        }

        if (summary.issues.isNotEmpty()) {
            TagGroup(
                title = "Issues",
                tags = summary.issues,
                style = ChipStyle.Error,
            )
        }

        if (summary.uncertainties.isNotEmpty()) {
            TagGroup(
                title = "Uncertainties",
                tags = summary.uncertainties,
                style = ChipStyle.Neutral,
            )
        }

        if (summary.checklist.isNotEmpty()) {
            TagGroup(
                title = "Checklist",
                tags = summary.checklist,
                style = ChipStyle.Success,
            )
        }
    }
}

@Composable
private fun FoodGrid(foods: List<HistoryFoodRenderModel>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
    ) {
        foods.forEach { food ->
            Surface(
                shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
                color = AppTheme.colors.surface.copy(alpha = 0.5f),
                tonalElevation = AppDimens.Size.xs,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.Spacing.xl3),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = food.title,
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.onSurface,
                        )
                        food.caloriesLabel?.let {
                            Text(
                                text = it,
                                style = AppTheme.typography.label,
                                color = AppTheme.colors.onSurface,
                            )
                        }
                    }
                    food.description?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                    ) {
                        food.quantityLabel?.let {
                            Text(
                                text = it,
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        food.macroLabel?.let {
                            Text(
                                text = it,
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                            )
                        }
                        food.confidenceLabel?.let {
                            Text(
                                text = it,
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentsRow(
    attachments: List<HistoryAttachmentRenderModel>,
    photoCountLabel: String?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
    ) {
        photoCountLabel?.let {
            Text(
                text = it,
                style = AppTheme.typography.label,
                color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            items(
                items = attachments,
                key = { it.id },
            ) { attachment ->
                Surface(
                    shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
                    border = BorderStroke(
                        AppDimens.BorderWidth.xs,
                        AppTheme.colors.outline.copy(alpha = 0.3f)
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppDimens.Size.xl16)
                            .background(surfaceVariantColor()),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (attachment.previewUrl != null) {
                            AsyncImage(
                                model = attachment.previewUrl,
                                contentDescription = attachment.description,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = attachment.description,
                                tint = AppTheme.colors.onSurface.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun surfaceVariantColor(): Color = MaterialTheme.colorScheme.surfaceVariant

@Preview
@Composable
private fun HistoryScreenPreview() {
    AppTheme {
        HistoryScreen(
            renderModel = HistorySampleDataProvider.randomRenderModel(),
            isLoading = false,
            onEntryClick = {},
        )
    }
}
