package com.sirelon.aicalories.features.media

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

data class PermissionDialogContent(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String,
)

@Composable
fun PermissionDialogs(
    controller: PermissionController,
    isIosDevice: Boolean,
    rationaleContent: PermissionDialogContent = PermissionDialogContent(
        title = "Camera permission needed",
        message = "We use the camera to capture meal photos. Please allow access so you can keep tracking.",
        confirmText = "Retry",
        dismissText = "Not now",
    ),
    settingsContentProvider: (Boolean) -> PermissionDialogContent = { ios ->
        PermissionDialogContent(
            title = "Allow camera access from settings",
            message = if (ios) {
                "Camera access is blocked. Open Settings > Privacy > Camera and enable AI Calories."
            } else {
                "Camera access is blocked. Please open the app settings and enable the camera permission."
            },
            confirmText = "Open settings",
            dismissText = "Cancel",
        )
    },
) {
    val permissionState by controller.uiState

    if (permissionState.showRationale) {
        AlertDialog(
            onDismissRequest = controller::dismissRationale,
            title = { Text(rationaleContent.title) },
            text = { Text(rationaleContent.message) },
            confirmButton = {
                TextButton(onClick = controller::retry) {
                    Text(rationaleContent.confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismissRationale) {
                    Text(rationaleContent.dismissText)
                }
            },
        )
    }

    if (permissionState.showSettings) {
        val settingsContent = settingsContentProvider(isIosDevice)
        AlertDialog(
            onDismissRequest = controller::dismissSettings,
            title = { Text(settingsContent.title) },
            text = { Text(settingsContent.message) },
            confirmButton = {
                TextButton(onClick = controller::openSettings) {
                    Text(settingsContent.confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismissSettings) {
                    Text(settingsContent.dismissText)
                }
            },
        )
    }
}
