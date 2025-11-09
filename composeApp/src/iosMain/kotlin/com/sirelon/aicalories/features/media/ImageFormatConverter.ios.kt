package com.sirelon.aicalories.features.media

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.readByteArray
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

private class IosImageFormatConverter : ImageFormatConverter {

    override suspend fun convert(
        platformContext: PlatformContext,
        file: KmpFile,
    ): KmpFile {
        val currentName = file.getName(platformContext)
        if (!currentName.isHeic()) {
            return file
        }

        val imageData = runCatching {
            file.readByteArray(platformContext)
        }.getOrNull()
            ?: return file

        val uiImage = UIImage(data = imageData.toNSData())
            ?: return file

        val jpegData = UIImageJPEGRepresentation(uiImage, 0.9)
            ?: return file

        val destinationName = currentName.toJpegFileName()
        val destinationUrl =
            NSURL.fileURLWithPath(
                path = NSTemporaryDirectory() + destinationName,
                isDirectory = false,
            )

        val success = jpegData.writeToURL(destinationUrl, true)
        return if (success) {
            KmpFile(url = destinationUrl, tempUrl = destinationUrl)
        } else {
            file
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

private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = size.toULong(),
        )
    }
