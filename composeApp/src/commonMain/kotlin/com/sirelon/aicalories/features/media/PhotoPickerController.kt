package com.sirelon.aicalories.features.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.sirelon.aicalories.camera.rememberCameraCaptureLauncher

data class PhotoPickerUiState(
    val files: List<KmpFile> = emptyList(),
    val fileNames: List<String> = emptyList(),
    val errorMessage: String? = null,
)

val PhotoPickerUiState.selectedFilesLabel: String?
    get() = fileNames.takeIf { it.isNotEmpty() }?.joinToString()

@Stable
interface PhotoPickerController {
    val uiState: State<PhotoPickerUiState>
    fun pickFromGallery()
    fun captureWithCamera()
    fun clearError()
}

@Composable
fun rememberPhotoPickerController(
    permissionController: PermissionController,
    type: FilePickerFileType = FilePickerFileType.Image,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Multiple,
): PhotoPickerController {
    val platformContext = LocalPlatformContext.current
    val filesState = remember { mutableStateListOf<KmpFile>() }
    val uiState = remember { mutableStateOf(PhotoPickerUiState()) }

    val cameraLauncher = rememberCameraCaptureLauncher { result ->
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
    }

    return remember(permissionController) {
        object : PhotoPickerController {
            override val uiState: State<PhotoPickerUiState> = uiState

            override fun pickFromGallery() {
                uiState.value = uiState.value.copy(errorMessage = null)
                permissionController.requestPermission {
                    filePickerLauncher.launch()
                }
            }

            override fun captureWithCamera() {
                uiState.value = uiState.value.copy(errorMessage = null)
                permissionController.requestPermission {
                    cameraLauncher.launch()
                }
            }

            override fun clearError() {
                if (uiState.value.errorMessage != null) {
                    uiState.value = uiState.value.copy(errorMessage = null)
                }
            }
        }
    }
}
