package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppCard
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ChipComponent
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.TagGroup
import com.sirelon.aicalories.designsystem.templates.CardWithTitle
import com.sirelon.aicalories.designsystem.templates.MacronutrientRow
import com.sirelon.aicalories.features.analyze.model.MealEntryUi
import com.sirelon.aicalories.features.analyze.model.MealSummaryUi
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.analyze.presentation.UploadItem
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyzeScreen(
    onBack: (() -> Unit)? = null,
    onResultConfirmed: (() -> Unit)? = null,
) {
    val viewModel: AnalyzeViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformContext = LocalPlatformContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionController = rememberPermissionController(permission = Permission.Camera)
    val uploads = state.uploads
    val hasReport = state.hasReport
    val hasResultData = state.result?.hasContent == true
    val canInteractWithPhotos = !hasReport && !state.isLoading
    val canAddMorePhotos = uploads.size < MAX_PHOTO_COUNT
    val canOpenPicker = canInteractWithPhotos && canAddMorePhotos
    val canSubmit = state.canSubmit && !hasReport

    val photoPicker =
        rememberPhotoPickerController(
            permissionController = permissionController,
            onResult = {
                viewModel.onEvent(
                    AnalyzeContract.AnalyzeEvent.UploadFilesResult(
                        platformContext = platformContext,
                        result = it,
                    )
                )
            },
        )
    var showSourceDialog by remember { mutableStateOf(false) }
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AnalyzeContract.AnalyzeEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    effect.message
                )
            }
        }
    }

    LaunchedEffect(hasReport) {
        if (hasReport) {
            showSourceDialog = false
        }
    }

    val windowDpSize = LocalWindowInfo.current.containerDpSize
    val isMediumWidth = windowDpSize.width >= 720.dp
    val useSplitLayout = isMediumWidth

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
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
                .padding(horizontal = AppDimens.Spacing.xl3),
            columns = GridCells.Fixed(if (useSplitLayout) 2 else 1),
            contentPadding = innerPadding,
            horizontalArrangement = arrangement,
            verticalArrangement = arrangement,
        ) {
            item {
                PhotosSection(
                    files = uploads,
                    interactionEnabled = canInteractWithPhotos,
                    canAddMore = canAddMorePhotos,
                    hasResult = hasReport,
                    onAddPhoto = {
                        if (canOpenPicker) {
                            showSourceDialog = true
                        }
                    },
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
                                title = "Total nutrition:",
                            ) {
                                MacronutrientRow(stats = it)
                            }
                        }
                    }


                    item {
                        CardWithTitle(title = "Detected insights") {
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

    if (showSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showSourceDialog = false },
            onPickFromGallery = {
                showSourceDialog = false
                photoPicker.pickFromGallery()
            },
            onCaptureWithCamera = {
                showSourceDialog = false
                photoPicker.captureWithCamera()
            },
            enabled = !state.isLoading,
        )
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
    val title = if (hasResult) "Analysis Result" else "Add Photos"
    val subtitle = if (hasResult) "Review detected items" else "Upload 1-3 images"
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
        tonalElevation = AppDimens.Size.xs,
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
                        text = "Confirm & Save",
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
                        text = if (hasResult) "Syncing…" else "Analyzing…",
                        style = AppTheme.typography.title,
                    )
                } else {
                    Text(
                        text = "Analyze",
                        style = AppTheme.typography.title,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosSection(
    modifier: Modifier = Modifier,
    files: Map<KmpFile, UploadItem>,
    interactionEnabled: Boolean,
    canAddMore: Boolean,
    hasResult: Boolean,
    onAddPhoto: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        color = AppTheme.colors.surface,
        tonalElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
            ) {
                Text(
                    text = "Photos",
                    style = AppTheme.typography.title,
                )
                val helperText = if (hasResult) {
                    "Photos stay locked while this meal is being analyzed."
                } else {
                    "Tap to add images"
                }
                Text(
                    text = helperText,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurface,
                )
            }

            PhotosGridComponent(
                files = files,
                canAddMore = interactionEnabled && canAddMore,
                interactionEnabled = interactionEnabled,
                onAddPhoto = onAddPhoto,
            )
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
        tonalElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = "Add description (optional)",
                style = AppTheme.typography.title,
            )
            Input(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                minLines = 4,
                label = "e.g., Breakfast at home, restaurant meal...",
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
        tonalElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = if (isLoading) "Crunching the numbers…" else "No summary yet",
                style = AppTheme.typography.label,
            )
            Text(
                text = if (isLoading) {
                    "Hang tight while we read the report from Supabase."
                } else {
                    "We’ll show the nutrition breakdown once the analysis is ready."
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
        tonalElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = "Summary",
                style = AppTheme.typography.label,
            )
            Text(
                text = summary.headline
                    ?: "We’ll highlight key nutrition facts here once available.",
                style = AppTheme.typography.body,
            )
            summary.qualityLabel?.let { label ->
                SummaryInsightsRow(label)
            }
            SummaryListSection(
                title = "Issues noted",
                items = summary.issues,
            )
            SummaryListSection(
                title = "Checklist",
                items = summary.checklist,
            )
            SummaryListSection(
                title = "Uncertainties",
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
        tonalElevation = AppDimens.Size.xs,
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
                    text = "• $item",
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
                    text = "Quantity: $qty",
                    style = AppTheme.typography.caption,
                )
            }
            MacronutrientRow(stats = entry.macroStats)

            TagGroup(title = "Tags", tags = entry.sourceTags)
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

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onCaptureWithCamera: () -> Unit,
    enabled: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add meal photos",
                style = AppTheme.typography.title,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            ) {
                TextButton(
                    onClick = onPickFromGallery,
                    enabled = enabled,
                ) {
                    Text("Pick from gallery")
                }
                TextButton(
                    onClick = onCaptureWithCamera,
                    enabled = enabled,
                ) {
                    Text("Take photo")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

private const val MAX_PHOTO_COUNT = 3
