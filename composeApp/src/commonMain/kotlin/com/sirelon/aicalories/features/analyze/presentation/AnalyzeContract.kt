package com.sirelon.aicalories.features.analyze.presentation

import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.data.AnalyzeResult

interface AnalyzeContract {

    data class AnalyzeState(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val result: AnalyzeResult? = null,
        val errorMessage: String? = null,
    )

    sealed interface AnalyzeEvent {
        data class PromptChanged(val value: String) : AnalyzeEvent

        data class UploadFile(val kmpFile: KmpFile) : AnalyzeEvent
        data object Submit : AnalyzeEvent
    }

    sealed interface AnalyzeEffect {
        data class ShowMessage(val message: String) : AnalyzeEffect
    }
}
