package com.sirelon.aicalories.features.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.sirelon.aicalories.camera.rememberCameraCaptureLauncher

data class PhotoPickerUiState(
    val hasPermission: Boolean = false,
    val files: List<KmpFile> = emptyList(),
    val fileNames: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showRationale: Boolean = false,
    val showSettings: Boolean = false,
)

val PhotoPickerUiState.selectedFilesLabel: String?
    get() = fileNames.takeIf { it.isNotEmpty() }?.joinToString()

@Stable
interface PhotoPickerController {
    val uiState: State<PhotoPickerUiState>
    fun pickFromGallery()
    fun captureWithCamera()
    fun dismissRationale()
    fun dismissSettings()
    fun retryPermissionRequest()
    fun openSettings()
    fun clearError()
}

private enum class PendingAction {
    Gallery,
    Camera,
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPhotoPickerController(
    isIosDevice: Boolean,
    type: FilePickerFileType = FilePickerFileType.Image,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Multiple,
    permission: Permission = Permission.Camera,
): PhotoPickerController {
    val platformContext = LocalPlatformContext.current

    val filesState = remember { mutableStateListOf<KmpFile>() }
    val uiState = remember { mutableStateOf(PhotoPickerUiState()) }
    val pendingAction = remember { mutableStateOf<PendingAction?>(null) }
    val lastAction = remember { mutableStateOf<PendingAction?>(null) }
    val denialAttempts = remember { mutableStateOf(0) }
    val lastHandledDenial = remember { mutableStateOf(0) }

    val cameraLauncher = rememberCameraCaptureLauncher { result ->
        pendingAction.value = null
        if (result.file != null) {
            filesState.add(result.file)
            uiState.value = uiState.value.copy(
                files = filesState.toList(),
                fileNames = filesState.mapNotNull { it.getName(platformContext) },
                errorMessage = null,
            )
        } else if (result.error != null && !result.cancelled) {
            uiState.value = uiState.value.copy(errorMessage = result.error)
        }
    }

    val filePickerLauncher = rememberFilePickerLauncher(
        type = type,
        selectionMode = selectionMode,
    ) { files ->
        filesState.clear()
        filesState.addAll(files)
        uiState.value = uiState.value.copy(
            files = filesState.toList(),
            fileNames = filesState.mapNotNull { it.getName(platformContext) },
            errorMessage = null,
        )
        pendingAction.value = null
    }

    val permissionState = rememberPermissionState(permission) { granted ->
        uiState.value = uiState.value.copy(
            hasPermission = granted,
            showRationale = false,
            showSettings = false,
        )
        if (granted) {
            denialAttempts.value = 0
            lastHandledDenial.value = 0
        } else {
            denialAttempts.value += 1
            pendingAction.value = null
        }
    }

    fun launch(action: PendingAction) {
        uiState.value = uiState.value.copy(
            errorMessage = null,
            showRationale = false,
            showSettings = false,
        )
        lastAction.value = action
        if (permissionState.status.isGranted) {
            pendingAction.value = null
            when (action) {
                PendingAction.Gallery -> filePickerLauncher.launch()
                PendingAction.Camera -> cameraLauncher.launch()
            }
        } else {
            pendingAction.value = action
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status, pendingAction.value) {
        val status = permissionState.status
        if (status.isGranted) {
            uiState.value = uiState.value.copy(
                hasPermission = true,
                showRationale = false,
                showSettings = false,
            )
            pendingAction.value?.let { action ->
                pendingAction.value = null
                when (action) {
                    PendingAction.Gallery -> filePickerLauncher.launch()
                    PendingAction.Camera -> cameraLauncher.launch()
                }
            }
        } else {
            uiState.value = uiState.value.copy(hasPermission = false)
        }
    }

    LaunchedEffect(permissionState.status, denialAttempts.value, isIosDevice) {
        val status = permissionState.status
        if (status.isGranted) {
            lastHandledDenial.value = 0
            return@LaunchedEffect
        }
        if (denialAttempts.value == 0 || denialAttempts.value == lastHandledDenial.value) return@LaunchedEffect

        val showSettings = isIosDevice || (status as? PermissionStatus.Denied)?.shouldShowRationale != true
        uiState.value = uiState.value.copy(
            showSettings = showSettings,
            showRationale = !showSettings,
        )
        lastHandledDenial.value = denialAttempts.value
    }

    return remember(isIosDevice) {
        object : PhotoPickerController {
            override val uiState: State<PhotoPickerUiState> = uiState

            override fun pickFromGallery() {
                launch(PendingAction.Gallery)
            }

            override fun captureWithCamera() {
                launch(PendingAction.Camera)
            }

            override fun dismissRationale() {
                uiState.value = uiState.value.copy(showRationale = false)
            }

            override fun dismissSettings() {
                uiState.value = uiState.value.copy(showSettings = false)
            }

            override fun retryPermissionRequest() {
                val action = lastAction.value ?: PendingAction.Gallery
                uiState.value = uiState.value.copy(showRationale = false, showSettings = false)
                pendingAction.value = action
                permissionState.launchPermissionRequest()
            }

            override fun openSettings() {
                uiState.value = uiState.value.copy(showSettings = false)
                permissionState.openAppSettings()
            }

            override fun clearError() {
                uiState.value = uiState.value.copy(errorMessage = null)
            }
        }
    }
}
