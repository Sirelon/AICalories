package com.sirelon.aicalories.features.analyze.presentation

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.common.BaseViewModel
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
) : BaseViewModel<AnalyzeContract.AnalyzeState, AnalyzeContract.AnalyzeEvent, AnalyzeContract.AnalyzeEffect>() {

    override fun initialState(): AnalyzeContract.AnalyzeState = AnalyzeContract.AnalyzeState()

    val images = mutableStateMapOf<KmpFile, Double>()

    // TODO:
    lateinit var platformContext: PlatformContext

    override fun onEvent(event: AnalyzeContract.AnalyzeEvent) {
        when (event) {
            is AnalyzeContract.AnalyzeEvent.PromptChanged -> setState {
                it.copy(
                    prompt = event.value,
                    errorMessage = null
                )
            }

            AnalyzeContract.AnalyzeEvent.Submit -> analyze()
            is AnalyzeContract.AnalyzeEvent.UploadFilesResult -> {
                event.result
                    .onSuccess { selectedFiles ->
                        selectedFiles.forEach { file ->
                            if (!images.containsKey(file)) {
                                images[file] = 0.0
                            }
                            viewModelScope.launch {
                                uploadFileFlow(file).collect()
                            }
                        }
                    }
                // TODO: handle errors
            }
        }
    }

    private fun uploadFileFlow(file: KmpFile): Flow<UploadStatus> = repository
        .uploadFile(platformContext = platformContext, file = file)
        .onEach { status ->
            val percent = when (status) {
                is UploadStatus.Progress -> {
                    if (status.contentLength > 0) {
                        (status.totalBytesSend.toDouble() / status.contentLength.toDouble()) * 100
                    } else 0.0
                }

                is UploadStatus.Success -> 100.0
            }

            images[file] = percent
        }

    private fun analyze() {
        val prompt = state.value.prompt.trim()
        if (prompt.isEmpty() && images.isEmpty()) {
            postEffect(effect = AnalyzeContract.AnalyzeEffect.ShowMessage("Add a description or at least one photo."))
            return
        }

        viewModelScope.launch {
            setState {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }


            repository
                .analyzeDescription(prompt = prompt.ifBlank { "Meal photo" })
                .onSuccess { result ->
                    setState {
                        it.copy(
                            prompt = "",
                            isLoading = false,
                            result = result,
                        )
                    }
                    postEffect(AnalyzeContract.AnalyzeEffect.ShowMessage("Analysis prepared."))
                }
                .onFailure { error ->
                    setState {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to analyze this meal.",
                        )
                    }
                }
        }
    }
}
