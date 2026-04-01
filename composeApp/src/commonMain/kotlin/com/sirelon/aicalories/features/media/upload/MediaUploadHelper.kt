package com.sirelon.aicalories.features.media.upload

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.sirelon.aicalories.features.media.ImageFormatConverter
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaUploadHelper(
    private val imageFormatConverter: ImageFormatConverter,
    private val repository: MediaUploadRepository,
) {

    /**
     * Validates and converts selected files without uploading them.
     * Store the returned list in your ViewModel and call [uploadPreparedFiles] when ready.
     */
    suspend fun prepareFiles(
        platformContext: PlatformContext,
        selectionResult: Result<List<KmpFile>>,
    ): Result<List<KmpFile>> {
        val selectedFiles = selectionResult.getOrElse { error ->
            return Result.failure(Exception(error.message ?: "Unable to access selected files."))
        }

        if (selectedFiles.isEmpty()) {
            return Result.failure(Exception("No files were selected."))
        }

        val unsupportedFiles = selectedFiles.filterNot {
            it.isSupportedOrConvertible(platformContext)
        }
        if (unsupportedFiles.isNotEmpty()) {
            return Result.failure(Exception("Only JPG, PNG, or WEBP images are supported."))
        }

        return runCatching {
            withContext(Dispatchers.Default) {
                selectedFiles.map { file ->
                    imageFormatConverter.convert(platformContext, file)
                }
            }
        }
    }

    /**
     * Uploads all previously prepared files to Supabase in parallel.
     * Call this when the user confirms (e.g. taps a confirm button).
     */
    fun uploadPreparedFiles(
        platformContext: PlatformContext,
        files: List<KmpFile>,
    ): Flow<MediaUploadUpdate> = channelFlow {
        if (files.isEmpty()) {
            send(MediaUploadUpdate.Error("No files to upload."))
            return@channelFlow
        }

        send(MediaUploadUpdate.Started)
        files.forEach { file ->
            send(MediaUploadUpdate.UploadStarted(file))
            launch {
                repository.uploadFile(platformContext, file)
                    .onEach { status ->
                        when (status) {
                            is UploadStatus.Progress -> {
                                send(
                                    MediaUploadUpdate.Progress(
                                        file = file,
                                        progress = status.toProgressPercent(),
                                    ),
                                )
                            }

                            is UploadStatus.Success -> {
                                send(
                                    MediaUploadUpdate.Success(
                                        file = file,
                                        uploadedFile = UploadedFile(
                                            id = status.response.id,
                                            path = status.response.path,
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                    .catch { error ->
                        send(MediaUploadUpdate.Failure(file, error.message ?: "Failed to upload file."))
                    }
                    .collect()
            }
        }
    }

    fun uploadSelectedFiles(
        platformContext: PlatformContext,
        selectionResult: Result<List<KmpFile>>,
    ): Flow<MediaUploadUpdate> = channelFlow {
        val prepared = prepareFiles(platformContext, selectionResult)
        val files = prepared.getOrElse { error ->
            send(MediaUploadUpdate.Error(error.message ?: "Unable to process selected files."))
            return@channelFlow
        }

        uploadPreparedFiles(platformContext, files).collect { send(it) }
    }
}

sealed interface MediaUploadUpdate {
    data object Started : MediaUploadUpdate
    data class AddPlaceholder(val file: KmpFile) : MediaUploadUpdate
    /** Emitted per file when batch upload begins. Flip item status to [UploadingStatus.Uploading]. */
    data class UploadStarted(val file: KmpFile) : MediaUploadUpdate
    data class Progress(
        val file: KmpFile,
        val progress: Double,
    ) : MediaUploadUpdate

    data class Success(
        val file: KmpFile,
        val uploadedFile: UploadedFile,
    ) : MediaUploadUpdate

    data class Failure(
        val file: KmpFile,
        val message: String,
    ) : MediaUploadUpdate

    data class Error(val message: String) : MediaUploadUpdate
}

private fun UploadStatus.Progress.toProgressPercent(): Double {
    return if (contentLength > 0) {
        (totalBytesSend.toDouble() / contentLength.toDouble()) * 100.0
    } else {
        0.0
    }
}

private val SUPPORTED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private val CONVERTIBLE_IMAGE_EXTENSIONS = setOf("heic", "heif")

private fun KmpFile.isSupportedOrConvertible(context: PlatformContext): Boolean {
    val extension = getFileExtension(context) ?: return false
    return extension in SUPPORTED_IMAGE_EXTENSIONS || extension in CONVERTIBLE_IMAGE_EXTENSIONS
}

private fun KmpFile.getFileExtension(context: PlatformContext): String? {
    val candidate = getName(context)
        ?: getPath(context)
        ?: return null
    val rawExtension = candidate.substringAfterLast('.', missingDelimiterValue = "")
    return rawExtension.takeIf { it.isNotBlank() }?.lowercase()
}
