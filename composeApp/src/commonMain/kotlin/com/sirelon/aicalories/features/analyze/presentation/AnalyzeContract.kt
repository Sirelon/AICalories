package com.sirelon.aicalories.features.analyze.presentation

import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import com.sirelon.aicalories.features.media.upload.UploadingItem

interface AnalyzeContract {

    data class AnalyzeState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val result: MealAnalysisUi? = null,
        val hasReport: Boolean = false,
        val errorMessage: String? = null,
        val uploads: Map<KmpFile, UploadingItem> = emptyMap(),
        val hasUploadFailures: Boolean = false,
    ) {
        val hasPendingUploads: Boolean
            get() = uploads.values.any { it.isPending || it.isUploading }

        val canSubmit: Boolean
            get() = !isLoading &&
                (prompt.isNotBlank() || uploads.isNotEmpty()) &&
                !hasPendingUploads
    }

    sealed interface AnalyzeEvent {
        data class PromptChanged(val value: String) : AnalyzeEvent

        data class UploadFilesResult(val result: Result<List<KmpFile>>) : AnalyzeEvent
        data object Submit : AnalyzeEvent
    }

    sealed interface AnalyzeEffect {
        data class ShowMessage(val message: String) : AnalyzeEffect
    }
}

