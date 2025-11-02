package com.sirelon.aicalories.features.analyze.ui

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.features.analyze.data.AnalyzeResult
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.platform.PlatformTargets
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    viewModel: AnalyzeViewModel = rememberAnalyzeViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isIosDevice = PlatformTargets.isIos()
    val permissionController = rememberPermissionController(
        permission = Permission.Camera,
        isIosDevice = isIosDevice,
    )
    val files = viewModel.images

    val photoPicker =
        rememberPhotoPickerController(
            permissionController = permissionController,
            onResult = {
                viewModel.onEvent(AnalyzeContract.AnalyzeEvent.UploadFilesResult(it))
            },
        )
    var showSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AnalyzeContract.AnalyzeEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .safeDrawingPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnalyzeTopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            AnalyzeBottomBar(
                enabled = state.prompt.isNotBlank() && !state.isLoading,
                isLoading = state.isLoading,
                onAnalyze = { viewModel.onEvent(AnalyzeContract.AnalyzeEvent.Submit) },
            )
        },
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PhotosSection(
                files = files.entries.toList(),
                onAddPhoto = {
                    if (!state.isLoading && files.size < MAX_PHOTO_COUNT) {
                        showSourceDialog = true
                    }
                },
                canAddMore = files.size < MAX_PHOTO_COUNT,
            )

            DescriptionSection(
                value = state.prompt,
                enabled = !state.isLoading,
                onValueChange = {
                    viewModel.onEvent(AnalyzeContract.AnalyzeEvent.PromptChanged(it))
                },
            )

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.error,
                )
            }

            state.result?.let { result ->
                ResultCard(result = result)
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
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
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    MediumFlexibleTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text("Add photos")
        },
        subtitle = {
            Text(text = "Upload 1-3 images")
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    )
}

@Composable
private fun AnalyzeBottomBar(
    enabled: Boolean,
    isLoading: Boolean,
    onAnalyze: () -> Unit,
) {
    Surface(
        color = AppTheme.colors.surface,
        contentColor = AppTheme.colors.onSurface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppDivider()
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = enabled,
                onClick = onAnalyze,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = AppTheme.colors.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
    files: List<Map.Entry<KmpFile, Double>>,
    onAddPhoto: () -> Unit,
    canAddMore: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Photos",
                style = AppTheme.typography.title,
            )
            Text(
                text = "Tap to add images",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurface,
            )
        }

        PhotosGrid(
            files = files,
            canAddMore = canAddMore,
            onAddPhoto = onAddPhoto,
        )
    }
}

@Composable
private fun PhotosGrid(
    files: List<Map.Entry<KmpFile, Double>>,
    canAddMore: Boolean,
    onAddPhoto: () -> Unit,
) {
    val totalSlots = when {
        canAddMore -> (files.size + 1).coerceAtLeast(3)
        else -> files.size.coerceAtLeast(3)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val rows = (totalSlots + GRID_COLUMNS - 1) / GRID_COLUMNS
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
            if (progress < 100.0) {
                UploadStatusIndicator(progress = progress)
            }
        }
    }
}

@Composable
private fun BoxScope.UploadStatusIndicator(
    progress: Double,
) {
    val percent = progress.coerceIn(0.0, 100.0)
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LinearProgressIndicator(
                progress = { (percent / 100f).toFloat() },
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

@Composable
private fun AddPhotoCell(
    modifier: Modifier,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                tint = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                contentDescription = "Add photo",
                modifier = Modifier.size(32.dp),
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Add description",
                style = AppTheme.typography.title,
            )
            Text(
                text = "(optional)",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurface,
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            minLines = 4,
            label = { Text("e.g., Breakfast at home, restaurant meal...") },
        )
    }
}

@Composable
private fun ResultCard(result: AnalyzeResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = result.summary,
                style = AppTheme.typography.title,
            )
            Text(
                text = result.recommendation,
                style = AppTheme.typography.body,
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
private const val MAX_PHOTO_COUNT = 3
