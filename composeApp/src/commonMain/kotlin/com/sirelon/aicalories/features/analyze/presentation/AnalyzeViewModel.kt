package com.sirelon.aicalories.features.analyze.presentation

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.common.BaseViewModel
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.model.MealAnalysisUi
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    private var observationJob: Job? = null
    private var observedFoodEntryId: Long? = null

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
                    result = null,
                    foodEntryId = null,
                )
            }

            observationJob?.cancel()
            observedFoodEntryId = null

            repository
                .analyzeDescription(prompt = prompt.ifBlank { "Meal photo" })
                .onSuccess { foodEntryId ->
                    images.clear()
                    observeFoodEntry(foodEntryId)
                    setState { current ->
                        current.copy(
                            prompt = "",
                            errorMessage = null,
                        )
                    }
                    postEffect(
                        AnalyzeContract.AnalyzeEffect.ShowMessage(
                            "Analysis started. You'll see results here shortly.",
                        ),
                    )
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

    fun observeFoodEntry(foodEntryId: Long) {
        if (foodEntryId <= 0L) return
        if (observedFoodEntryId == foodEntryId) return

        observationJob?.cancel()
        observedFoodEntryId = foodEntryId
        observationJob = viewModelScope.launch {
            setState { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null,
                    foodEntryId = foodEntryId,
                    result = current.result?.takeIf { it.hasContent },
                )
            }

            repository
                .observeAnalysis(foodEntryId)
                .catch { error ->
                    setState { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load analysis report.",
                        )
                    }
                }
                .collect { report ->
                    setState { current ->
                        current.copy(
                            result = report,
                            isLoading = report == null,
                            errorMessage = if (report == null) current.errorMessage else null,
                            foodEntryId = foodEntryId,
                        )
                    }
                }
        }
    }
}
