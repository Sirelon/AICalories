package com.sirelon.aicalories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import aicalories.composeapp.generated.resources.Res
import aicalories.composeapp.generated.resources.compose_multiplatform
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext as CoilLocalPlatformContext
import com.mohamedrejeb.calf.core.LocalPlatformContext as CalfLocalPlatformContext
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.readByteArray
import com.sirelon.aicalories.designsystem.AiCaloriesTheme
import com.sirelon.aicalories.designsystem.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun App() {


    AiCaloriesTheme {
        val greeting = remember { Greeting().greet() }
        val platformName = remember { getPlatform().name }
        val isIosDevice = remember(platformName) {
            platformName.contains("iOS", ignoreCase = true) ||
                    platformName.contains("iPadOS", ignoreCase = true)
        }

        val calfPlatformContext = CalfLocalPlatformContext.current
        val coilPlatformContext = CoilLocalPlatformContext.current
        val imageLoader = remember(coilPlatformContext) {
            ImageLoader.Builder(coilPlatformContext).build()
        }
        val scope = rememberCoroutineScope()

        var permissionDeniedCount by remember { mutableStateOf(0) }
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var pendingPickerLaunch by remember { mutableStateOf(false) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var selectedImageName by remember { mutableStateOf<String?>(null) }
        var pickerError by remember { mutableStateOf<String?>(null) }
        var isReadingImage by remember { mutableStateOf(false) }

        val filePickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Image,
            selectionMode = FilePickerSelectionMode.Single,
        ) { files ->
            pendingPickerLaunch = false
            val file = files.firstOrNull()
            if (file == null) {
                pickerError = null
                return@rememberFilePickerLauncher
            }
            scope.launch {
                isReadingImage = true
                pickerError = null
                try {
                    val bytes = withContext(Dispatchers.Default) {
                        file.readByteArray(calfPlatformContext)
                    }
                    selectedImageBytes = bytes
                    selectedImageName =
                        file.getName(calfPlatformContext) ?: "Selected meal photo"
                } catch (error: Throwable) {
                    selectedImageBytes = null
                    selectedImageName = null
                    pickerError = error.message ?: "Failed to load the selected image."
                } finally {
                    isReadingImage = false
                }
            }
        }

        val cameraPermissionState = rememberPermissionState(Permission.Camera) { granted ->
            if (granted) {
                permissionDeniedCount = 0
            } else {
                permissionDeniedCount += 1
                pendingPickerLaunch = false
            }
        }

        val permissionStatus = cameraPermissionState.status
        val permissionGranted = permissionStatus.isGranted

        LaunchedEffect(permissionGranted, pendingPickerLaunch) {
            if (permissionGranted && pendingPickerLaunch) {
                pendingPickerLaunch = false
                filePickerLauncher.launch()
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
                enabled = !isReadingImage,
                onClick = {
                    showRationaleDialog = false
                    showSettingsDialog = false
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
            if (pickerError != null) {
                Text(
                    text = pickerError!!,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.error,
                )
            }
            if (isReadingImage) {
                CircularProgressIndicator()
            }
            AnimatedVisibility(permissionGranted && selectedImageBytes != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    selectedImageName?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.label,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    AsyncImage(
                        model = selectedImageBytes,
                        imageLoader = imageLoader,
                        contentDescription = "Selected meal photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text(
                        text = "Compose: $greeting",
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.success,
                    )
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
