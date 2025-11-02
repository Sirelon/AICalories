package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeContract
import com.sirelon.aicalories.features.analyze.presentation.AnalyzeViewModel
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.platform.PlatformTargets
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyzeScreen(
    viewModel: AnalyzeViewModel = rememberAnalyzeViewModel(),
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

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AnalyzeContract.AnalyzeEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Analyze meal",
                style = AppTheme.typography.headline,
            )
            Text(
                text = "Describe what you ate and we will give you a nutritional summary.",
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurface,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.prompt,
                onValueChange = { viewModel.onEvent(AnalyzeContract.AnalyzeEvent.PromptChanged(it)) },
                minLines = 4,
                enabled = !state.isLoading,
                label = { Text("Meal details") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = photoPicker::pickFromGallery,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Pick from gallery")
                }
                Button(
                    onClick = photoPicker::captureWithCamera,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Take photo")
                }
            }
//
//            photoUi.errorMessage?.let { error ->
//                Text(
//                    text = error,
//                    style = AppTheme.typography.caption,
//                    color = AppTheme.colors.error,
//                )
//            }

            if (files.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Uploads",
                            style = AppTheme.typography.title,
                        )

                        ImagesCollectionComponent(
                            files = files,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                        )
                    }
                }
            }

            Button(
                enabled = state.prompt.isNotBlank() && !state.isLoading,
                onClick = { viewModel.onEvent(AnalyzeContract.AnalyzeEvent.Submit) },
            ) {
                Text("Analyze")
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.error,
                )
            }

            state.result?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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

            PermissionDialogs(
                controller = permissionController,
                isIosDevice = isIosDevice,
            )
        }
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
