package com.sirelon.aicalories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import aicalories.composeapp.generated.resources.Res
import aicalories.composeapp.generated.resources.compose_multiplatform
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.sirelon.aicalories.designsystem.AiCaloriesTheme
import com.sirelon.aicalories.designsystem.AppTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun App() {
    AiCaloriesTheme {
        val platformName = remember { getPlatform().name }
        val isIosDevice = remember(platformName) {
            platformName.contains("iOS", ignoreCase = true) ||
                platformName.contains("iPadOS", ignoreCase = true)
        }

        var permissionDeniedCount by remember { mutableStateOf(0) }
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }

        val cameraPermissionState = rememberPermissionState(Permission.Camera) { granted ->
            if (granted) {
                permissionDeniedCount = 0
            } else {
                permissionDeniedCount += 1
            }
        }

        val permissionStatus = cameraPermissionState.status
        val permissionGranted = permissionStatus.isGranted

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

            val deniedStatus = permissionStatus as? PermissionStatus.Denied ?: return@LaunchedEffect
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
                enabled = !permissionGranted,
                onClick = {
                    showRationaleDialog = false
                    showSettingsDialog = false
                    cameraPermissionState.launchPermissionRequest()
                },
            ) {
                Text(
                    text = if (permissionGranted) "Permission already granted" else "Grant camera permission",
                )
            }
            AnimatedVisibility(permissionGranted) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
