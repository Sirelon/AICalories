package com.sirelon.aicalories.features.seller.ad.generate_ad

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.media.upload.UploadingItem

interface GenerateAdContract {

    data class GenerateAdState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val uploads: Map<KmpFile, UploadingItem> = emptyMap(),
    ) {
        val hasPendingUploads: Boolean
            get() = uploads.values.any { !it.isUploaded }

        val canSubmit: Boolean
            get() = !isLoading
                    && uploads.isNotEmpty()
                    && !hasPendingUploads
    }

    sealed interface GenerateAdEvent {
        data class PromptChanged(val value: String) : GenerateAdEvent

        data class UploadFilesResult(
            val platformContext: PlatformContext,
            val result: Result<List<KmpFile>>,
        ) : GenerateAdEvent

        data object Submit : GenerateAdEvent
    }

    sealed interface GenerateAdEffect {
        data class ShowMessage(val message: String) : GenerateAdEffect
    }
}