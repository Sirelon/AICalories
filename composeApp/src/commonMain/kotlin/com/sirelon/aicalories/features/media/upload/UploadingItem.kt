package com.sirelon.aicalories.features.media.upload

data class UploadingItem(
    val progress: Double = 0.0,
    val isUploading: Boolean = false,
    val uploadedFile: UploadedFile? = null,
) {
    val isPending: Boolean get() = !isUploading && uploadedFile == null
    val isUploaded: Boolean get() = uploadedFile != null
}
