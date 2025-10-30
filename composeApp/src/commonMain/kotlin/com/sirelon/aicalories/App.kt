package com.sirelon.aicalories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.sirelon.aicalories.camera.rememberCameraCaptureLauncher
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.di.appModule
import com.sirelon.aicalories.di.networkModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import com.mohamedrejeb.calf.core.LocalPlatformContext as CalfLocalPlatformContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory {
        ImageLoader.Builder(it)
            .components {
                add(KmpFileFetcher.Factory())
            }
            .build()
    }


    KoinApplication(
        application = { modules(appModule, networkModule) },
    ) {
        AppTheme {
            val greeting: Greeting = koinInject()
            val greetingMessage = remember(greeting) { greeting.greet() }
            val platformName = remember { getPlatform().name }
            val isIosDevice = remember(platformName) {
                platformName.contains("iOS", ignoreCase = true) ||
                        platformName.contains("iPadOS", ignoreCase = true)
            }

            val calfPlatformContext = CalfLocalPlatformContext.current

            var permissionDeniedCount by remember { mutableStateOf(0) }
            var showRationaleDialog by remember { mutableStateOf(false) }
            var showSettingsDialog by remember { mutableStateOf(false) }
            var pendingPickerLaunch by remember { mutableStateOf(false) }
            var pendingCameraLaunch by remember { mutableStateOf(false) }
            var selectedImageName by remember { mutableStateOf<String?>(null) }
            var pickerError by remember { mutableStateOf<String?>(null) }

            val filesState = remember { mutableStateListOf<KmpFile>() }

            val cameraLauncher = rememberCameraCaptureLauncher { result ->
                pendingCameraLaunch = false
                if (result.file != null) {
                    filesState.add(result.file)
                    selectedImageName = filesState
                        .mapNotNull { it.getName(calfPlatformContext) }
                        .joinToString()
                    pickerError = null
                } else if (result.error != null && !result.cancelled) {
                    pickerError = result.error
                }
            }

            val filePickerLauncher = rememberFilePickerLauncher(
                type = FilePickerFileType.Image,
                selectionMode = FilePickerSelectionMode.Multiple,
            ) { files ->
                filesState.clear()
                filesState.addAll(files)
                selectedImageName = filesState
                    .mapNotNull { it.getName(calfPlatformContext) }
                    .joinToString()

                pendingPickerLaunch = false
                pickerError = null
            }

            val cameraPermissionState = rememberPermissionState(Permission.Camera) { granted ->
                if (granted) {
                    permissionDeniedCount = 0
                } else {
                    permissionDeniedCount += 1
                    pendingPickerLaunch = false
                    pendingCameraLaunch = false
                }
            }

            val permissionStatus = cameraPermissionState.status
            val permissionGranted = permissionStatus.isGranted

            LaunchedEffect(permissionGranted, pendingPickerLaunch, pendingCameraLaunch) {
                if (!permissionGranted) return@LaunchedEffect
                if (pendingPickerLaunch) {
                    pendingPickerLaunch = false
                    filePickerLauncher.launch()
                }
                if (pendingCameraLaunch) {
                    pendingCameraLaunch = false
                    cameraLauncher.launch()
                }
            }

            LaunchedEffect(permissionStatus, permissionDeniedCount, isIosDevice) {
                if (permissionGranted) {
                    if (permissionDeniedCount != 0) {
                        permissionDeniedCount = 0
                    }
                    showRationaleDialog = false
                    showSettingsDialog = false
                    return@LaunchedEffect
                }

                showRationaleDialog = false
                showSettingsDialog = false

                val deniedStatus =
                    permissionStatus as? PermissionStatus.Denied ?: return@LaunchedEffect
                if (permissionDeniedCount == 0) return@LaunchedEffect

                if (isIosDevice) {
                    showSettingsDialog = true
                } else {
                    if (!deniedStatus.shouldShowRationale || permissionDeniedCount > 1) {
                        showSettingsDialog = true
                    } else {
                        showRationaleDialog = true
                    }
                }
            }

            Column(
                modifier = Modifier
                    .background(AppTheme.colors.background)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = if (permissionGranted) "Camera permission granted" else "Camera permission required",
                    style = AppTheme.typography.title,
                )
                Text(
                    text = if (permissionGranted) {
                        "Thanks! You can capture your meals now."
                    } else {
                        "AI Calories needs camera access to analyse your meals."
                    },
                    style = AppTheme.typography.body,
                )
                Button(
                    onClick = {
                        showRationaleDialog = false
                        showSettingsDialog = false
                        pickerError = null
                        if (permissionGranted) {
                            pendingPickerLaunch = false
                            filePickerLauncher.launch()
                        } else {
                            pendingPickerLaunch = true
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                ) {
                    Text(
                        text = if (permissionGranted) "Pick meal photo" else "Grant camera permission",
                    )
                }
                Button(
                    onClick = {
                        showRationaleDialog = false
                        showSettingsDialog = false
                        pickerError = null
                        if (permissionGranted) {
                            pendingCameraLaunch = false
                            cameraLauncher.launch()
                        } else {
                            pendingCameraLaunch = true
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                ) {
                    Text(
                        text = if (permissionGranted) "Capture meal photo" else "Grant camera permission",
                    )
                }
                if (pickerError != null) {
                    Text(
                        text = pickerError!!,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.error,
                    )
                }
                AnimatedVisibility(permissionGranted) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                    ) {
                        selectedImageName?.let {
                            Text(
                                text = it,
                                style = AppTheme.typography.label,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        filesState.forEach { file ->
                            AsyncImage(
                                model = file,
                                contentDescription = "Selected meal photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            if (showRationaleDialog) {
                AlertDialog(
                    onDismissRequest = { showRationaleDialog = false },
                    title = { Text("Camera permission needed") },
                    text = {
                        Text("We use the camera to capture meal photos. Please allow access so you can keep tracking.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRationaleDialog = false
                                pendingPickerLaunch = true
                                cameraPermissionState.launchPermissionRequest()
                            },
                        ) {
                            Text("Retry")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRationaleDialog = false }) {
                            Text("Not now")
                        }
                    },
                )
            }

            if (showSettingsDialog) {
                val settingsMessage = if (isIosDevice) {
                    "Camera access is blocked. Open Settings > Privacy > Camera and enable AI Calories."
                } else {
                    "Camera access is blocked. Please open the app settings and enable the camera permission."
                }
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Allow camera access from settings") },
                    text = { Text(settingsMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSettingsDialog = false
                                cameraPermissionState.openAppSettings()
                            },
                        ) {
                            Text("Open settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }
        }
    }
}
