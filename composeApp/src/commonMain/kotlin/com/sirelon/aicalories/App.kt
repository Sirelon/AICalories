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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.di.appModule
import com.sirelon.aicalories.di.networkModule
import com.sirelon.aicalories.platform.PlatformTargets
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.features.media.selectedFilesLabel
import org.koin.compose.KoinApplication

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
            val isIosDevice = PlatformTargets.isIos()

            val permissionController = rememberPermissionController(
                permission = Permission.Camera,
                isIosDevice = isIosDevice,
            )
            val permissionUi = permissionController.uiState.value

            val files = remember { mutableStateListOf<KmpFile>() }
            val photoPicker = rememberPhotoPickerController(
                permissionController = permissionController,
                onResult = {
                    it.onSuccess {
                        files.clear()
                        files.addAll(it)
                    }.onFailure {
                        // TODO:
                    }
                },
            )
            val permissionGranted = permissionUi.hasPermission

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
                    onClick = photoPicker::pickFromGallery,
                ) {
                    Text(
                        text = if (permissionGranted) "Pick meal photo" else "Grant camera permission",
                    )
                }
                Button(
                    onClick = photoPicker::captureWithCamera,
                ) {
                    Text(
                        text = if (permissionGranted) "Capture meal photo" else "Grant camera permission",
                    )
                }
//                photoUi.errorMessage?.let { message ->
//                    Text(
//                        text = message,
//                        style = AppTheme.typography.caption,
//                        color = AppTheme.colors.error,
//                    )
//                }
                AnimatedVisibility(permissionGranted) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
                    ) {
                        files.forEach { file ->
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

            if (permissionUi.showRationale) {
                AlertDialog(
                    onDismissRequest = { permissionController.dismissRationale() },
                    title = { Text("Camera permission needed") },
                    text = {
                        Text("We use the camera to capture meal photos. Please allow access so you can keep tracking.")
                    },
                    confirmButton = {
                        TextButton(onClick = { permissionController.retry() }) {
                            Text("Retry")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { permissionController.dismissRationale() }) {
                            Text("Not now")
                        }
                    },
                )
            }

            if (permissionUi.showSettings) {
                AlertDialog(
                    onDismissRequest = { permissionController.dismissSettings() },
                    title = { Text("Allow camera access from settings") },
                    text = {
                        val message = if (isIosDevice) {
                            "Camera access is blocked. Open Settings > Privacy > Camera and enable AI Calories."
                        } else {
                            "Camera access is blocked. Please open the app settings and enable the camera permission."
                        }
                        Text(message)
                    },
                    confirmButton = {
                        TextButton(onClick = { permissionController.openSettings() }) {
                            Text("Open settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { permissionController.dismissSettings() }) {
                            Text("Cancel")
                        }
                    },
                )
            }
        }
    }
}
