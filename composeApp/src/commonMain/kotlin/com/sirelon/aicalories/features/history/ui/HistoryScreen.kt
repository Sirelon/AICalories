@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.sirelon.aicalories.features.history.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme

@Composable
fun HistoryScreen(
    renderModel: HistoryScreenRenderModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onEntryClick: (HistoryEntryRenderModel) -> Unit = {},
    onEmptyStateAction: (() -> Unit)? = null,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        topBar = {
            HistoryTopBar(
                header = renderModel.header,
                onBack = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        if (renderModel.groupedEntries.isEmpty()) {
            HistoryEmptyState(
                model = renderModel.emptyState ?: HistoryEmptyStateRenderModel(
                    title = "No history yet",
                    description = "Your analysed meals will appear here once you capture and submit them.",
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onActionClick = onEmptyStateAction,
            )
            return@Scaffold
        }

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
                HistoryHeaderSection(renderModel.header)
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

@Composable
private fun HistoryTopBar(
    header: HistoryHeaderRenderModel,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = header.title,
                    style = AppTheme.typography.headline,
                )
                header.subtitle?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun HistoryHeaderSection(header: HistoryHeaderRenderModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (header.insights.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            ) {
                header.insights.forEach { insight ->
                    TagPill(
                        text = insight,
                        containerColor = AppTheme.colors.success.copy(alpha = 0.15f),
                        contentColor = AppTheme.colors.success,
                        iconColor = AppTheme.colors.success,
                        icon = Icons.Filled.CheckCircle,
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
            val x = if (chartPoints.size == 1) width / 2f else width * index / (chartPoints.size - 1)
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
            val x = if (chartPoints.size == 1) width / 2f else width * index / (chartPoints.size - 1)
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
    val borderColor = if (isHighlighted) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.3f)
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
                    entry.summary?.qualityLabel?.let {
                        TagPill(
                            text = it,
                            containerColor = AppTheme.colors.success.copy(alpha = 0.18f),
                            contentColor = AppTheme.colors.success,
                            icon = Icons.Filled.CheckCircle,
                            iconColor = AppTheme.colors.success,
                        )
                    }
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

            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                ) {
                    entry.tags.forEach { tag ->
                        TagPill(
                            text = tag,
                            containerColor = AppTheme.colors.surface.copy(alpha = 0.6f),
                            contentColor = AppTheme.colors.onSurface,
                            icon = Icons.Filled.Info,
                            iconColor = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }
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
                toneColor = AppTheme.colors.error,
                icon = Icons.Filled.ErrorOutline,
            )
        }

        if (summary.uncertainties.isNotEmpty()) {
            TagGroup(
                title = "Uncertainties",
                tags = summary.uncertainties,
                toneColor = AppTheme.colors.onSurface,
                icon = Icons.Filled.Info,
            )
        }

        if (summary.checklist.isNotEmpty()) {
            TagGroup(
                title = "Checklist",
                tags = summary.checklist,
                toneColor = AppTheme.colors.success,
                icon = Icons.Filled.CheckCircle,
            )
        }
    }
}

@Composable
private fun TagGroup(
    title: String,
    tags: List<String>,
    toneColor: Color,
    icon: ImageVector,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.label,
            color = toneColor,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            tags.forEach { tag ->
                TagPill(
                    text = tag,
                    containerColor = toneColor.copy(alpha = 0.15f),
                    contentColor = toneColor,
                    icon = icon,
                    iconColor = toneColor,
                )
            }
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
                    border = BorderStroke(AppDimens.BorderWidth.xs, AppTheme.colors.outline.copy(alpha = 0.3f)),
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

@Composable
private fun TagPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    iconColor: Color = contentColor,
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl2),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppDimens.Spacing.xl3,
                vertical = AppDimens.Spacing.m,
            ),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(AppDimens.Size.xl - AppDimens.Size.xs),
                )
            }
            Text(
                text = text,
                style = AppTheme.typography.caption,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HistoryEmptyState(
    model: HistoryEmptyStateRenderModel,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.background)
            .padding(AppDimens.Spacing.xl8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = model.title,
            style = AppTheme.typography.title,
            color = AppTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.Spacing.xl3))
        Text(
            text = model.description,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        model.actionLabel?.let { actionLabel ->
            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl4))
            Button(
                onClick = { onActionClick?.invoke() },
                enabled = onActionClick != null,
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    AppTheme {
        HistoryScreen(
            renderModel = previewHistoryScreenRenderModel(),
            onEntryClick = {},
        )
    }
}

private fun previewHistoryScreenRenderModel(): HistoryScreenRenderModel {
    val attachments = listOf(
        HistoryAttachmentRenderModel(
            id = "file-1",
            previewUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400",
            description = "Lunch plate",
        ),
        HistoryAttachmentRenderModel(
            id = "file-2",
            previewUrl = "https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=400",
            description = "Smoothie",
        ),
    )
    val foods = listOf(
        HistoryFoodRenderModel(
            id = "food-1",
            title = "Grilled Chicken",
            description = "Skinless, herbs",
            quantityLabel = "180 g",
            caloriesLabel = "290 kcal",
            macroLabel = "P32 • F6 • C0",
            confidenceLabel = "from image • 94%",
            fromImage = true,
        ),
        HistoryFoodRenderModel(
            id = "food-2",
            title = "Brown Rice",
            quantityLabel = "1 cup",
            caloriesLabel = "210 kcal",
            macroLabel = "P5 • F2 • C45",
            confidenceLabel = "from note • 88%",
            fromNote = true,
        ),
    )
    val summary = HistoryReportSummaryRenderModel(
        advice = "Great balance! Add leafy greens for extra micronutrients.",
        qualityLabel = "High quality",
        issues = listOf("Added oil not detected", "Fiber lower than target"),
        uncertainties = listOf("Sauce ingredients unconfirmed"),
        checklist = listOf("Protein source logged", "Hydration noted"),
    )

    val entry = HistoryEntryRenderModel(
        id = 1,
        dateLabel = "Nov 2, 2025",
        timeLabel = "12:32 PM",
        caloriesLabel = "710 kcal",
        note = "Post workout lunch with extra avocado.",
        attachments = attachments,
        foods = foods,
        macros = MacroBreakdownRenderModel(
            calories = "710 kcal",
            protein = "48 g",
            fat = "22 g",
            carbs = "78 g",
        ),
        summary = summary,
        tags = listOf("Contains allergens", "Tracked via AI"),
        photoCountLabel = "2 photos",
        confidenceLabel = "Confidence 92%",
    )

    val breakfastEntry = entry.copy(
        id = 2,
        dateLabel = "Nov 2, 2025",
        timeLabel = "08:05 AM",
        caloriesLabel = "430 kcal",
        note = "Oatmeal with berries and nuts.",
        attachments = attachments.take(1),
        summary = summary.copy(
            advice = "Consider adding a protein shake after the workout.",
            issues = listOf("Protein target not met"),
            uncertainties = emptyList(),
        ),
        tags = listOf("Fiber rich"),
        foods = listOf(
            HistoryFoodRenderModel(
                id = "food-3",
                title = "Oatmeal",
                description = "Rolled oats cooked with oat milk",
                quantityLabel = "1 bowl",
                caloriesLabel = "280 kcal",
                macroLabel = "P10 • F6 • C46",
                confidenceLabel = "from image • 91%",
                fromImage = true,
            ),
            HistoryFoodRenderModel(
                id = "food-4",
                title = "Blueberries",
                quantityLabel = "40 g",
                caloriesLabel = "45 kcal",
                macroLabel = "P1 • F0 • C11",
                confidenceLabel = "from note • 85%",
                fromNote = true,
            ),
        ),
    )

    return HistoryScreenRenderModel(
        header = HistoryHeaderRenderModel(
            title = "History & Insights",
            subtitle = "Track past analyses",
            insights = listOf("Consistency +8%", "Avg 2050 kcal"),
        ),
        weeklySummary = WeeklyCaloriesRenderModel(
            title = "Calories this week",
            totalLabel = "9,850 kcal",
            changeLabel = "+4% vs last week",
            targetLabel = "Target 9,450 kcal",
            points = listOf(
                CaloriePointRenderModel("mon", "Mon", 1850, "1.8k"),
                CaloriePointRenderModel("tue", "Tue", 2100, "2.1k"),
                CaloriePointRenderModel("wed", "Wed", 1950, "1.9k"),
                CaloriePointRenderModel("thu", "Thu", 2200, "2.2k"),
                CaloriePointRenderModel("fri", "Fri", 1900, "1.9k"),
                CaloriePointRenderModel("sat", "Sat", 2400, "2.4k"),
                CaloriePointRenderModel("sun", "Sun", 2050, "2.0k"),
            ),
        ),
        groupedEntries = listOf(
            HistoryGroupRenderModel(
                groupId = "nov-2",
                dayLabel = "Nov 2, 2025",
                entries = listOf(entry, breakfastEntry),
            ),
            HistoryGroupRenderModel(
                groupId = "nov-1",
                dayLabel = "Nov 1, 2025",
                entries = listOf(
                    entry.copy(
                        id = 3,
                        dateLabel = "Nov 1, 2025",
                        timeLabel = "07:45 PM",
                        caloriesLabel = "780 kcal",
                        tags = listOf("Needs review"),
                        summary = summary.copy(issues = listOf("Missing micronutrients snapshot")),
                    ),
                ),
            ),
        ),
        highlightedEntryId = 1,
    )
}
