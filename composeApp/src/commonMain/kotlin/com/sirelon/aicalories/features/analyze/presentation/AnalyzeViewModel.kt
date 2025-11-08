package com.sirelon.aicalories.features.analyze.presentation

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository.UploadedFile
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
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

    private var observationJob: Job? = null
    private var observedFoodEntryId: Long? = null

    override fun onEvent(event: AnalyzeContract.AnalyzeEvent) {
        when (event) {
            is AnalyzeContract.AnalyzeEvent.PromptChanged -> {
                setState {
                    it.copy(
                        prompt = event.value,
                        errorMessage = null,
                    )
                }
            }

            AnalyzeContract.AnalyzeEvent.Submit -> analyze()
            is AnalyzeContract.AnalyzeEvent.UploadFilesResult -> {
                event.result
                    .onSuccess { selectedFiles ->
                        selectedFiles.forEach { file ->
                           addUploadPlaceholder(file)
                            viewModelScope.launch {
                                uploadFileFlow(
                                    platformContext = event.platformContext,
                                    file = file,
                                ).collect()
                            }
                        }
                    }
                // TODO: handle errors
            }
        }
    }

    private fun uploadFileFlow(
        platformContext: PlatformContext,
        file: KmpFile,
    ): Flow<UploadStatus> = repository
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

            when (status) {
                is UploadStatus.Progress -> updateUpload(
                    file = file,
                ) { item ->
                    item.copy(progress = percent)
                }

                is UploadStatus.Success -> updateUpload(
                    file = file,
                ) { item ->
                    item.copy(
                        progress = percent,
                        uploadedFile = UploadedFile(
                            id = status.response.id,
                            path = status.response.path,
                        ),
                    )
                }
            }
        }

    private fun analyze() {
        val prompt = state.value.prompt.trim()
        val uploadsSnapshot = state.value.uploads
        if (prompt.isEmpty() && uploadsSnapshot.isEmpty()) {
            postEffect(effect = AnalyzeContract.AnalyzeEffect.ShowMessage("Add a description or at least one photo."))
            return
        }

        if (state.value.hasPendingUploads) {
            postEffect(effect = AnalyzeContract.AnalyzeEffect.ShowMessage("Please wait until all uploads finish."))
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

            val uploadedFiles = state.value.uploads.values.mapNotNull { it.uploadedFile }
            val note = prompt.ifBlank { null }

            repository
                .createFoodEntry(note = note, files = uploadedFiles)
                .onFailure { error ->
                    setState {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to create food entry.",
                        )
                    }
                }.onSuccess { foodEntryId ->
                    setState {
                        it.copy(
                            prompt = "",
                            uploads = emptyMap(),
                        )
                    }
                    observeFoodEntry(foodEntryId)
                    postEffect(
                        AnalyzeContract.AnalyzeEffect.ShowMessage(
                            "Analysis started. You'll see results here shortly.",
                        ),
                    )
                }
        }
    }

    fun attachFoodEntry(foodEntryId: Long) {
        observeFoodEntry(foodEntryId)
    }

    private fun observeFoodEntry(foodEntryId: Long) {
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

    private fun addUploadPlaceholder(file: KmpFile) {
        setState { current ->
            if (current.uploads.containsKey(file)) {
                current
            } else {
                current.copy(
                    uploads = current.uploads + (file to UploadItem())
                )
            }
        }
    }

    private fun updateUpload(
        file: KmpFile,
        reducer: (UploadItem) -> UploadItem,
    ) {
        setState { current ->
            val existing = current.uploads[file] ?: return@setState current
            current.copy(
                uploads = current.uploads + (file to reducer(existing))
            )
        }
    }
}
