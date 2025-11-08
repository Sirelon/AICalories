package com.sirelon.aicalories.features.analyze.presentation

import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi

interface AnalyzeContract {

    data class AnalyzeState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val result: MealAnalysisUi? = null,
        val foodEntryId: Long? = null,
        val errorMessage: String? = null,
    )

    sealed interface AnalyzeEvent {
        data class PromptChanged(val value: String) : AnalyzeEvent

        data class UploadFilesResult(val result: Result<List<KmpFile>>) : AnalyzeEvent
        data object Submit : AnalyzeEvent
    }

    sealed interface AnalyzeEffect {
        data class ShowMessage(val message: String) : AnalyzeEffect
    }
}
