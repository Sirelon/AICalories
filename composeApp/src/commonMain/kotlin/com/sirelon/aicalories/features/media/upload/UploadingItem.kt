package com.sirelon.aicalories.features.media.upload

data class UploadingItem(
    val progress: Double = 0.0,
    val uploadedFile: UploadedFile? = null,
) {
    val isUploaded: Boolean get() = uploadedFile != null
}