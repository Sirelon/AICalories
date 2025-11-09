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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
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
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ChipComponent
import com.sirelon.aicalories.designsystem.TagGroup
import com.sirelon.aicalories.features.analyze.model.MacroStatUi
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import com.sirelon.aicalories.features.analyze.model.MealEntryUi
import com.sirelon.aicalories.features.analyze.model.MealSummaryUi
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.analyze.presentation.UploadItem
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
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

    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val windowDpSize = LocalWindowInfo.current.containerDpSize
    val hasSeparatingHinge = adaptiveInfo.windowPosture.separatingVerticalHingeBounds.isNotEmpty()
    val isMediumWidth = windowDpSize.width >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp
    val useSplitLayout = isMediumWidth && !hasSeparatingHinge

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
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(innerPadding),
            columns = GridCells.Fixed(if (useSplitLayout) 2 else 1)
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

            item {
                AnalyzeFields(
                    showResult = hasReport,
                    state = state,
                    onEvent = viewModel::onEvent,
                )
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
private fun AnalyzeFields(
    showResult: Boolean,
    state: AnalyzeContract.AnalyzeState,
    onEvent: (AnalyzeContract.AnalyzeEvent) -> Unit,
) {
    if (!showResult) {
        DescriptionSection(
            value = state.prompt,
            enabled = !state.isLoading,
            onValueChange = {
                onEvent(AnalyzeContract.AnalyzeEvent.PromptChanged(it))
            },
        )
    } else {
        AnalyzeResultSection(
            result = state.result,
            isLoading = state.isLoading,
        )
    }

    state.errorMessage?.let { error ->
        ErrorMessage(text = error)
    }

    if (state.isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                minLines = 4,
                label = { Text("e.g., Breakfast at home, restaurant meal...") },
            )
        }
    }
}

@Composable
private fun AnalyzeResultSection(
    result: MealAnalysisUi?,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
    ) {
        Text(
            text = "Detected insights",
            style = AppTheme.typography.title,
        )
        if (result == null) {
            PendingAnalysisCard(isLoading = isLoading)
            return
        }

        SummaryCard(summary = result.summary)
        EntriesSection(entries = result.entries)
    }
}

@Composable
private fun PendingAnalysisCard(
    isLoading: Boolean,
) {
    Surface(
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
    Surface(
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
private fun EntriesSection(
    entries: List<MealEntryUi>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
    ) {
        Text(
            text = "Detected items (${entries.size})",
            style = AppTheme.typography.label,
        )
        if (entries.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
                tonalElevation = AppDimens.Size.xs,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.Spacing.xl5),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
                ) {
                    Text(
                        text = "No food items detected yet",
                        style = AppTheme.typography.body,
                    )
                    Text(
                        text = "We’ll populate this list once the detector extracts ingredients from your photos and note.",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            entries.forEachIndexed { index, entry ->
                EntryCard(entry = entry)
                if (index < entries.lastIndex) {
                    AppDivider()
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: MealEntryUi,
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        tonalElevation = AppDimens.Size.xs,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = entry.title,
                    style = AppTheme.typography.title,
                )
                entry.confidence?.let { confidence ->
                    ChipComponent(data = confidence)
                }
            }
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
    }
}

@Composable
private fun MacronutrientRow(
    stats: List<MacroStatUi>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        stats.forEach { stat ->
            NutrientStat(
                label = stat.label,
                value = stat.value,
            )
        }
    }
}

@Composable
private fun NutrientStat(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = AppTheme.typography.title,
        )
        Text(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
        )
    }
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
