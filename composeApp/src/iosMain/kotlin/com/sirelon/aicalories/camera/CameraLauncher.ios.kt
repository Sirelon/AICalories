package com.sirelon.aicalories.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSUUID
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureModePhoto
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceTypeCamera
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.presentViewController
import platform.UIKit.dismissViewControllerAnimated
import platform.UIKit.isSourceTypeAvailable
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraCaptureLauncher(
    onResult: (CameraCaptureResult) -> Unit,
): CameraLauncher {
    val currentController = LocalUIViewController.current
    val delegate = remember { CameraCaptureDelegate() }
    delegate.onResult = onResult

    return remember(currentController, delegate) {
        CameraLauncherImpl {
            if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypeCamera)) {
                onResult(CameraCaptureResult(error = "Camera not available.", cancelled = true))
                return@CameraLauncherImpl
            }

            val picker =
                UIImagePickerController().apply {
                    sourceType = UIImagePickerControllerSourceTypeCamera
                    cameraCaptureMode = UIImagePickerControllerCameraCaptureModePhoto
                    delegate = delegate
                }
            delegate.onResult = onResult

            currentController.presentViewController(
                picker,
                true,
                null,
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class CameraCaptureDelegate :
    platform.darwin.NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    var onResult: ((CameraCaptureResult) -> Unit)? = null

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = image?.jpegData()
        val fileName = "camera_${NSUUID().UUIDString}.jpg"
        val tempDirectory = NSTemporaryDirectory()
        val fileUrl = if (data != null) NSURL.fileURLWithPath(tempDirectory + fileName) else null

        val writeSucceeded = if (data != null && fileUrl != null) {
            data.writeToURL(fileUrl, true)
        } else {
            false
        }

        val result =
            if (writeSucceeded && fileUrl != null) {
                @OptIn(InternalCalfApi::class)
                CameraCaptureResult(
                    file = KmpFile(url = fileUrl, tempUrl = fileUrl),
                    displayName = fileName,
                )
            } else {
                CameraCaptureResult(error = "Failed to capture photo.")
            }

        onResult?.invoke(result)
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onResult?.invoke(CameraCaptureResult(cancelled = true))
        picker.dismissViewControllerAnimated(true, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.jpegData() = UIImageJPEGRepresentation(this, 0.9)

private class CameraLauncherImpl(
    private val onLaunch: () -> Unit,
) : CameraLauncher {
    override fun launch() {
        onLaunch()
    }
}
