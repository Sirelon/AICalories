package com.sirelon.aicalories.features.analyze.presentation

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository.UploadedFile
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi

interface AnalyzeContract {

    data class AnalyzeState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val result: MealAnalysisUi? = null,
        val foodEntryId: Long? = null,
        val errorMessage: String? = null,
        val uploads: Map<KmpFile, UploadItem> = emptyMap(),
    ) {
        val hasPendingUploads: Boolean
            get() = uploads.values.any { !it.isUploaded }

        val canSubmit: Boolean
            get() = !isLoading &&
                (prompt.isNotBlank() || uploads.isNotEmpty()) &&
                !hasPendingUploads
    }

    sealed interface AnalyzeEvent {
        data class PromptChanged(val value: String) : AnalyzeEvent

        data class UploadFilesResult(
            val platformContext: PlatformContext,
            val result: Result<List<KmpFile>>,
        ) : AnalyzeEvent
        data object Submit : AnalyzeEvent
    }

    sealed interface AnalyzeEffect {
        data class ShowMessage(val message: String) : AnalyzeEffect
    }
}

data class UploadItem(
    val progress: Double = 0.0,
    val uploadedFile: UploadedFile? = null,
) {
    val isUploaded: Boolean get() = uploadedFile != null
}
