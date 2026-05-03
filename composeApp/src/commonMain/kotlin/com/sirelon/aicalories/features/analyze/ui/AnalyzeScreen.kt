package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppCard
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ChipComponent
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.TagGroup
import com.sirelon.aicalories.designsystem.templates.CardWithTitle
import com.sirelon.aicalories.designsystem.templates.MacronutrientRow
import com.sirelon.aicalories.features.analyze.model.MealEntryUi
import com.sirelon.aicalories.features.analyze.model.MealSummaryUi
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.features.media.ui.PhotosSection
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.add_description
import com.sirelon.aicalories.generated.resources.add_photos_title
import com.sirelon.aicalories.generated.resources.analysis_result_title
import com.sirelon.aicalories.generated.resources.analyze_button
import com.sirelon.aicalories.generated.resources.analyzing
import com.sirelon.aicalories.generated.resources.bullet_item
import com.sirelon.aicalories.generated.resources.checklist
import com.sirelon.aicalories.generated.resources.confirm_and_save
import com.sirelon.aicalories.generated.resources.crunching_numbers
import com.sirelon.aicalories.generated.resources.description_placeholder
import com.sirelon.aicalories.generated.resources.detected_insights
import com.sirelon.aicalories.generated.resources.issues_noted
import com.sirelon.aicalories.generated.resources.loading_report
import com.sirelon.aicalories.generated.resources.no_summary_yet
import com.sirelon.aicalories.generated.resources.nutrition_breakdown_loading
import com.sirelon.aicalories.generated.resources.nutrition_facts_loading
import com.sirelon.aicalories.generated.resources.quantity_format
import com.sirelon.aicalories.generated.resources.review_detected_items
import com.sirelon.aicalories.generated.resources.summary
import com.sirelon.aicalories.generated.resources.syncing
import com.sirelon.aicalories.generated.resources.tags
import com.sirelon.aicalories.generated.resources.total_nutrition
import com.sirelon.aicalories.generated.resources.uncertainties
import com.sirelon.aicalories.generated.resources.upload_images_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyzeScreen(
    onBack: (() -> Unit)? = null,
    onResultConfirmed: (() -> Unit)? = null,
) {
    val viewModel: AnalyzeViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionController = rememberPermissionController(permission = Permission.Camera)

    val uploads = state.uploads
    val hasReport = state.hasReport
    val hasResultData = state.result?.hasContent == true
    val canSubmit = state.canSubmit && !hasReport

    val photoPicker = rememberPhotoPickerController(
        permissionController = permissionController,
        onResult = {
            viewModel.onEvent(AnalyzeContract.AnalyzeEvent.UploadFilesResult(result = it))
        },
    )
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is AnalyzeContract.AnalyzeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    val windowDpSize = LocalWindowInfo.current.containerDpSize
    val isMediumWidth = windowDpSize.width >= 720.dp
    val useSplitLayout = isMediumWidth

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnalyzeTopBar(
                hasResult = hasReport,
                onBack = onBack,
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        bottomBar = {
            AnalyzeBottomBar(
                hasResult = hasReport,
                canSubmit = canSubmit,
                canConfirm = hasResultData,
                isLoading = state.isLoading,
                onAnalyze = { viewModel.onEvent(AnalyzeContract.AnalyzeEvent.Submit) },
                onConfirm = onResultConfirmed,
            )
        },
    ) { innerPadding ->
        val arrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = AppDimens.Spacing.xl3),
            columns = GridCells.Fixed(if (useSplitLayout) 2 else 1),
            contentPadding = innerPadding,
            horizontalArrangement = arrangement,
            verticalArrangement = arrangement,
        ) {
            item {
                PhotosSection(
                    files = uploads,
                    onTakePhotoClick = photoPicker::captureWithCamera,
                    onUploadClick = photoPicker::pickFromGallery,
                )
            }

            if (hasReport) {
                val result = state.result
                if (result == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PendingAnalysisCard(isLoading = state.isLoading)
                    }
                } else {
                    result.combinedMacroStats?.let {
                        item {
                            CardWithTitle(
                                title = stringResource(Res.string.total_nutrition),
                            ) {
                                MacronutrientRow(stats = it)
                            }
                        }
                    }


                    item {
                        CardWithTitle(title = stringResource(Res.string.detected_insights)) {
                            SummaryCard(summary = result.summary)
                        }
                    }

                    items(items = result.entries) {
                        EntryCard(entry = it)
                    }
                }
            } else {
                item {
                    DescriptionSection(
                        value = state.prompt,
                        enabled = !state.isLoading,
                        onValueChange = {
                            viewModel.onEvent(AnalyzeContract.AnalyzeEvent.PromptChanged(it))
                        },
                    )
                }

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                state.errorMessage?.let { error ->
                    ErrorMessage(text = error)
                }
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    PermissionDialogs(
        controller = permissionController,
    )
}

@Composable
private fun AnalyzeTopBar(
    hasResult: Boolean,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val title =
        if (hasResult) stringResource(Res.string.analysis_result_title) else stringResource(Res.string.add_photos_title)
    val subtitle =
        if (hasResult) stringResource(Res.string.review_detected_items) else stringResource(Res.string.upload_images_hint)
    AppLargeAppBar(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AnalyzeBottomBar(
    hasResult: Boolean,
    canSubmit: Boolean,
    canConfirm: Boolean,
    isLoading: Boolean,
    onAnalyze: () -> Unit,
    onConfirm: (() -> Unit)?,
) {
    Surface(
        color = AppTheme.colors.surface,
        contentColor = AppTheme.colors.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.Spacing.xl6,
                    vertical = AppDimens.Spacing.xl4,
                ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
        ) {
            AppDivider()
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.Size.xl13),
                enabled = when {
                    hasResult && !isLoading -> canConfirm && onConfirm != null
                    hasResult && isLoading -> false
                    else -> canSubmit
                },
                onClick = {
                    if (hasResult && !isLoading) {
                        onConfirm?.invoke()
                    } else if (!hasResult) {
                        onAnalyze()
                    }
                },
            ) {
                if (hasResult && !isLoading) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing.xl3))
                    Text(
                        text = stringResource(Res.string.confirm_and_save),
                        style = AppTheme.typography.title,
                    )
                } else if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppDimens.Size.xl6),
                        strokeWidth = AppDimens.BorderWidth.s,
                        color = AppTheme.colors.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing.xl3))
                    Text(
                        text = if (hasResult) stringResource(Res.string.syncing) else stringResource(
                            Res.string.analyzing
                        ),
                        style = AppTheme.typography.title,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.analyze_button),
                        style = AppTheme.typography.title,
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        color = AppTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = stringResource(Res.string.add_description),
                style = AppTheme.typography.title,
            )
            Input(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                minLines = 4,
                label = stringResource(Res.string.description_placeholder),
            )
        }
    }
}

@Composable
private fun PendingAnalysisCard(
    isLoading: Boolean,
) {
    AppCard(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = if (isLoading) stringResource(Res.string.crunching_numbers) else stringResource(
                    Res.string.no_summary_yet
                ),
                style = AppTheme.typography.label,
            )
            Text(
                text = if (isLoading) {
                    stringResource(Res.string.loading_report)
                } else {
                    stringResource(Res.string.nutrition_breakdown_loading)
                },
                style = AppTheme.typography.body,
            )
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SummaryCard(
    summary: MealSummaryUi,
) {
    AppCard(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = stringResource(Res.string.summary),
                style = AppTheme.typography.label,
            )
            Text(
                text = summary.headline ?: stringResource(Res.string.nutrition_facts_loading),
                style = AppTheme.typography.body,
            )
            summary.qualityLabel?.let { label ->
                SummaryInsightsRow(label)
            }
            SummaryListSection(
                title = stringResource(Res.string.issues_noted),
                items = summary.issues,
            )
            SummaryListSection(
                title = stringResource(Res.string.checklist),
                items = summary.checklist,
            )
            SummaryListSection(
                title = stringResource(Res.string.uncertainties),
                items = summary.uncertainties,
            )
        }
    }
}

@Composable
private fun SummaryInsightsRow(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl),
        color = AppTheme.colors.primary.copy(alpha = 0.1f),
    ) {
        Text(
            modifier = Modifier
                .padding(
                    horizontal = AppDimens.Spacing.xl3,
                    vertical = AppDimens.Spacing.xs,
                ),
            text = text,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.primary,
        )
    }
}

@Composable
private fun SummaryListSection(
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.label,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
        ) {
            items.forEach { item ->
                Text(
                    text = stringResource(Res.string.bullet_item, item),
                    style = AppTheme.typography.body,
                )
            }
        }
    }
}

@Composable
private fun EntryCard(entry: MealEntryUi) {
    val spacing = AppDimens.Spacing.xl

    CardWithTitle(
        spacing = spacing,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = entry.title,
                )

                entry.confidence?.let { chip ->
                    ChipComponent(data = chip)
                }
            }
        },
        content = {
            entry.description?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.body,
                )
            }
            entry.quantityText?.let { qty ->
                Text(
                    text = stringResource(Res.string.quantity_format, qty),
                    style = AppTheme.typography.caption,
                )
            }
            MacronutrientRow(stats = entry.macroStats)

            TagGroup(title = stringResource(Res.string.tags), tags = entry.sourceTags)
        }
    )
}

@Composable
private fun ErrorMessage(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        color = AppTheme.colors.error.copy(alpha = 0.1f),
        contentColor = AppTheme.colors.error,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl4),
            text = text,
            style = AppTheme.typography.caption,
        )
    }
}
