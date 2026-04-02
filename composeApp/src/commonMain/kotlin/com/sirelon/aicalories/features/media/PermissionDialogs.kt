package com.sirelon.aicalories.features.media

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.*
import com.sirelon.aicalories.platform.PlatformTargets
import org.jetbrains.compose.resources.stringResource

data class PermissionDialogContent(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String,
)

@Composable
fun PermissionDialogs(
    controller: PermissionController,
    isIosDevice: Boolean = PlatformTargets.isIos(),
    rationaleContent: PermissionDialogContent? = null,
    settingsContentProvider: ((Boolean) -> PermissionDialogContent)? = null,
) {
    val resolvedRationaleContent = rationaleContent ?: PermissionDialogContent(
        title = stringResource(Res.string.camera_rationale_title),
        message = stringResource(Res.string.camera_rationale_message),
        confirmText = stringResource(Res.string.retry),
        dismissText = stringResource(Res.string.not_now),
    )
    val resolvedSettingsContent = (settingsContentProvider ?: { ios ->
        PermissionDialogContent(
            title = stringResource(Res.string.camera_settings_title),
            message = if (ios) {
                stringResource(Res.string.camera_settings_message_ios)
            } else {
                stringResource(Res.string.camera_settings_message_android)
            },
            confirmText = stringResource(Res.string.open_settings),
            dismissText = stringResource(Res.string.cancel),
        )
    })(isIosDevice)
    val permissionState by controller.uiState

    if (permissionState.showRationale) {
        AlertDialog(
            onDismissRequest = controller::dismissRationale,
            title = { Text(resolvedRationaleContent.title) },
            text = { Text(resolvedRationaleContent.message) },
            confirmButton = {
                TextButton(onClick = controller::retry) {
                    Text(resolvedRationaleContent.confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismissRationale) {
                    Text(resolvedRationaleContent.dismissText)
                }
            },
        )
    }

    if (permissionState.showSettings) {
        AlertDialog(
            onDismissRequest = controller::dismissSettings,
            title = { Text(resolvedSettingsContent.title) },
            text = { Text(resolvedSettingsContent.message) },
            confirmButton = {
                TextButton(onClick = controller::openSettings) {
                    Text(resolvedSettingsContent.confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = controller::dismissSettings) {
                    Text(resolvedSettingsContent.dismissText)
                }
            },
        )
    }
}
