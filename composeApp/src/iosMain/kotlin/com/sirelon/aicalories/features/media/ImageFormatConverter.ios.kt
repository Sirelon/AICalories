package com.sirelon.aicalories.features.media

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.readByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Foundation.writeToURL

actual fun imageFormatConverter(): ImageFormatConverter = IosImageFormatConverter()

@OptIn(InternalCalfApi::class)
private class IosImageFormatConverter : ImageFormatConverter {

    override suspend fun convert(
        platformContext: PlatformContext,
        file: KmpFile,
    ): KmpFile {
        return withContext(Dispatchers.Main) {
            val currentName = file.getName(platformContext)
            if (!currentName.isHeic()) {
                return@withContext file
            }

            val imageData = runCatching {
                file.readByteArray(platformContext)
            }.getOrNull()
                ?: return@withContext file

            val uiImage = UIImage(data = imageData.toNSData())
                ?: return@withContext file

            val jpegData = UIImageJPEGRepresentation(uiImage, 0.9)
                ?: return@withContext file

            val destinationName = currentName.toJpegFileName()
            val destinationUrl =
                NSURL.fileURLWithPath(
                    path = NSTemporaryDirectory() + destinationName,
                    isDirectory = false,
                )

            val success = jpegData.writeToURL(destinationUrl, true)
            if (success) {
                KmpFile(url = destinationUrl, tempUrl = destinationUrl)
            } else {
                file
            }
        }
    }
}

private fun String?.isHeic(): Boolean {
    if (this == null) return false
    val lower = lowercase()
    return lower.endsWith(".heic") || lower.endsWith(".heif")
}

private fun String?.toJpegFileName(): String {
    val base = this?.substringBeforeLast('.', missingDelimiterValue = this ?: "") ?: ""
    val safeBase = base.ifBlank { "image_${NSUUID().UUIDString}" }
    return "$safeBase.jpg"
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = size.toULong(),
        )
    }
