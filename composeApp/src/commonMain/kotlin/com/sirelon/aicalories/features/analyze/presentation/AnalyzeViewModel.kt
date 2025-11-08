package com.sirelon.aicalories.features.analyze.presentation

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository.UploadedFile
import com.sirelon.aicalories.features.analyze.data.ReportAnalysisUiMapper
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
    private val mapper: ReportAnalysisUiMapper,
) : BaseViewModel<AnalyzeContract.AnalyzeState, AnalyzeContract.AnalyzeEvent, AnalyzeContract.AnalyzeEffect>() {

    override fun initialState(): AnalyzeContract.AnalyzeState = AnalyzeContract.AnalyzeState()


    private val foodEntryIdEmitter = MutableStateFlow<Long?>(null)


    init {
        foodEntryIdEmitter
            .filterNotNull()
            .onEach {
                setState { current ->
                    current.copy(
                        isLoading = true,
                        errorMessage = null,
                        hasReport = false,
                        result = current.result?.takeIf { it.hasContent },
                    )
                }
            }
            .flatMapLatest(repository::observeAnalysis)
            .map { mapper.toUi(it.summary, it.entries) }
            .onEach { report ->
                setState { current ->
                    current.copy(
                        result = report,
                        isLoading = false,
                        errorMessage = null,
                        hasReport = true,
                    )
                }
            }
            .catch { error ->
                setState { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load analysis report.",
                    )
                }
            }
            .launchIn(viewModelScope)
    }


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
                        if (selectedFiles.isEmpty()) {
                            showError("No files were selected.")
                            return@onSuccess
                        }

                        setState { it.copy(errorMessage = null) }

                        selectedFiles.forEach { file ->
                            addUploadPlaceholder(file)
                            viewModelScope.launch {
                                uploadFileFlow(
                                    platformContext = event.platformContext,
                                    file = file,
                                )
                                    .catch { error -> handleUploadFailure(file, error) }
                                    .collect()
                            }
                        }
                    }
                    .onFailure { error ->
                        showError(error.message ?: "Unable to access selected files.")
                    }
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

                is UploadStatus.Success -> {
                    updateUpload(
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

        setState {
            it.copy(isLoading = true)
        }

        postEffect(
            AnalyzeContract.AnalyzeEffect.ShowMessage(
                "Analysis started. You'll see results here shortly.",
            ),
        )

        viewModelScope.launch {
            val uploadedFiles = state.value.uploads.values.mapNotNull { it.uploadedFile }
            val note = prompt.ifBlank { null }

            val foodEntryId = repository
                .createFoodEntry(note = note, files = uploadedFiles)
                .onFailure { error ->
                    setState {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to create food entry.",
                        )
                    }
                }
                .getOrElse { return@launch }

            foodEntryIdEmitter.emit(foodEntryId)

            repository
                .requestAnalysis(foodEntryId)
                .onFailure { error ->
                    setState {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to start analysis.",
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

    private fun handleUploadFailure(file: KmpFile, error: Throwable) {
        val message = error.message ?: "Failed to upload file."
        setState { current ->
            current.copy(
                errorMessage = message,
                uploads = current.uploads - file,
            )
        }
        postEffect(AnalyzeContract.AnalyzeEffect.ShowMessage(message))
    }

    private fun showError(message: String) {
        setState { it.copy(errorMessage = message) }
        postEffect(AnalyzeContract.AnalyzeEffect.ShowMessage(message))
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
