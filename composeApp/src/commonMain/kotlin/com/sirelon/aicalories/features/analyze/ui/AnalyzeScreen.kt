package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MediumFlexibleTopAppBar
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
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.features.analyze.data.AnalyzeResult
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.platform.PlatformTargets
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AnalyzeScreen(
    viewModel: AnalyzeViewModel = rememberAnalyzeViewModel(),
    onBack: (() -> Unit)? = null,
    onResultConfirmed: (() -> Unit)? = null,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isIosDevice = PlatformTargets.isIos()
    val permissionController = rememberPermissionController(
        permission = Permission.Camera,
        isIosDevice = isIosDevice,
    )
    val files = viewModel.images
    val fileEntries = files.entries.toList()
    val hasResult = state.result != null
    val canInteractWithPhotos = !hasResult && !state.isLoading
    val canAddMorePhotos = files.size < MAX_PHOTO_COUNT
    val canOpenPicker = canInteractWithPhotos && canAddMorePhotos
    val canSubmit = !state.isLoading && (state.prompt.isNotBlank() || fileEntries.isNotEmpty())

    val photoPicker =
        rememberPhotoPickerController(
            permissionController = permissionController,
            onResult = {
                viewModel.onEvent(AnalyzeContract.AnalyzeEvent.UploadFilesResult(it))
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

    LaunchedEffect(hasResult) {
        if (hasResult) {
            showSourceDialog = false
        }
    }

    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val windowDpSize = currentWindowDpSize()
    val hasSeparatingHinge = adaptiveInfo.windowPosture.separatingVerticalHingeBounds.isNotEmpty()
    val isMediumWidth = windowDpSize.width >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp
    val useSplitLayout = isMediumWidth && !hasSeparatingHinge

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnalyzeTopBar(
                hasResult = hasResult,
                onBack = onBack,
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        bottomBar = {
            AnalyzeBottomBar(
                hasResult = hasResult,
                canSubmit = canSubmit,
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
                    files = fileEntries,
                    interactionEnabled = canInteractWithPhotos,
                    canAddMore = canAddMorePhotos,
                    hasResult = hasResult,
                    onAddPhoto = {
                        if (canOpenPicker) {
                            showSourceDialog = true
                        }
                    },
                )
            }

            item {
                AnalyzeFields(hasResult, state, viewModel::onEvent)
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
        isIosDevice = isIosDevice,
    )
}

@Composable
private fun AnalyzeFields(
    hasResult: Boolean,
    state: AnalyzeContract.AnalyzeState,
    onEvent: (AnalyzeContract.AnalyzeEvent) -> Unit,
) {
    if (!hasResult) {
        DescriptionSection(
            value = state.prompt,
            enabled = !state.isLoading,
            onValueChange = {
                onEvent(AnalyzeContract.AnalyzeEvent.PromptChanged(it))
            },
        )
    } else {
        state.result?.let {
            AnalyzeResultSection(result = it)
        }
    }

    state.errorMessage?.let { error ->
        ErrorMessage(text = error)
    }

    if (state.isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun rememberAnalyzeViewModel(): AnalyzeViewModel = koinViewModel<AnalyzeViewModel>()
    .also { viewModel ->
        val context = LocalPlatformContext.current
        LaunchedEffect(Unit) {
            viewModel.platformContext = context
        }
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnalyzeTopBar(
    hasResult: Boolean,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val title = if (hasResult) "Analysis Result" else "Add Photos"
    val subtitle = if (hasResult) "Review detected items" else "Upload 1-3 images"
    MediumFlexibleTopAppBar(
        subtitle = {
            Text(text = subtitle)
        },
        title = {
            Text(text = title)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
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
private fun AnalyzeBottomBar(
    hasResult: Boolean,
    canSubmit: Boolean,
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
            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.12f))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.Size.xl13),
                enabled = if (hasResult) onConfirm != null else canSubmit,
                onClick = {
                    if (hasResult) {
                        onConfirm?.invoke()
                    } else {
                        onAnalyze()
                    }
                },
            ) {
                if (hasResult) {
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
                        text = "Analyzing…",
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
    files: List<Map.Entry<KmpFile, Double>>,
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
                    "Uploads are locked after analysis."
                } else {
                    "Tap to add images"
                }
                Text(
                    text = helperText,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurface,
                )
            }

            PhotosGrid(
                files = files,
                canAddMore = interactionEnabled && canAddMore,
                interactionEnabled = interactionEnabled,
                onAddPhoto = onAddPhoto,
            )
        }
    }
}

@Composable
private fun PhotosGrid(
    files: List<Map.Entry<KmpFile, Double>>,
    canAddMore: Boolean,
    interactionEnabled: Boolean,
    onAddPhoto: () -> Unit,
) {
    val totalSlots = when {
        canAddMore -> (files.size + 1).coerceAtLeast(MIN_GRID_SIZE)
        else -> files.size.coerceAtLeast(MIN_GRID_SIZE)
    }

    val rows = (totalSlots + GRID_COLUMNS - 1) / GRID_COLUMNS

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
            ) {
                repeat(GRID_COLUMNS) { columnIndex ->
                    val slotIndex = rowIndex * GRID_COLUMNS + columnIndex
                    if (slotIndex >= totalSlots) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        )
                    } else {
                        val entry = files.getOrNull(slotIndex)
                        if (entry != null) {
                            PhotoPreview(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                progress = entry.value,
                                file = entry.key,
                            )
                        } else if (canAddMore) {
                            AddPhotoCell(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                enabled = interactionEnabled,
                                onClick = onAddPhoto,
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPreview(
    modifier: Modifier,
    progress: Double,
    file: KmpFile,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        tonalElevation = AppDimens.Size.xs,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            AsyncImage(
                model = file,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (progress < COMPLETE_PERCENTAGE) {
                UploadStatusIndicator(progress = progress)
            }
        }
    }
}

@Composable
private fun BoxScope.UploadStatusIndicator(
    progress: Double,
) {
    val percent = progress.coerceIn(0.0, COMPLETE_PERCENTAGE)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .align(Alignment.BottomCenter),
        color = Color.Black.copy(alpha = 0.4f),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.Spacing.xl,
                    vertical = AppDimens.Spacing.m,
                ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
        ) {
            LinearProgressIndicator(
                progress = { (percent / COMPLETE_PERCENTAGE).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Text(
                text = "${percent.toInt()}%",
                style = AppTheme.typography.caption,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhotoCell(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        border = BorderStroke(
            width = AppDimens.BorderWidth.l,
            color = AppTheme.colors.outline.copy(alpha = if (enabled) 1f else 0.4f),
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                tint = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                contentDescription = "Add photo",
                modifier = Modifier.size(AppDimens.Size.xl8),
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
    result: AnalyzeResult,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
    ) {
        Text(
            text = "Detected insights",
            style = AppTheme.typography.title,
        )
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
                    text = result.summary,
                    style = AppTheme.typography.body,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
            border = BorderStroke(
                width = AppDimens.BorderWidth.s,
                color = AppTheme.colors.primary.copy(alpha = 0.3f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.Spacing.xl5),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
            ) {
                Text(
                    text = "Recommendation",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.primary,
                )
                Text(
                    text = result.recommendation,
                    style = AppTheme.typography.body,
                )
            }
        }
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

private const val GRID_COLUMNS = 3
private const val MIN_GRID_SIZE = 3
private const val MAX_PHOTO_COUNT = 3
private const val COMPLETE_PERCENTAGE = 100.0
