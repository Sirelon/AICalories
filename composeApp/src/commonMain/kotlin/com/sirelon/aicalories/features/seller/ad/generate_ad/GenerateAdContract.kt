package com.sirelon.aicalories.features.seller.ad.generate_ad

import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.media.upload.UploadingItem
import com.sirelon.aicalories.features.seller.ad.Advertisement

interface GenerateAdContract {

    data class GenerateAdState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val uploads: Map<KmpFile, UploadingItem> = emptyMap(),
    ) {
        val canSubmit: Boolean
            get() = !isLoading && uploads.isNotEmpty()
    }

    sealed interface GenerateAdEvent {
        data class PromptChanged(val value: String) : GenerateAdEvent

        data class UploadFilesResult(val result: Result<List<KmpFile>>) : GenerateAdEvent

        data object Submit : GenerateAdEvent
    }

    sealed interface GenerateAdEffect {
        data class ShowMessage(val message: String) : GenerateAdEffect
        data class OpenAdPreview(val ad: Advertisement) : GenerateAdEffect
    }
}