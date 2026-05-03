package com.sirelon.aicalories.features.analyze.presentation

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import com.sirelon.aicalories.features.analyze.data.ReportAnalysisUiMapper
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.media.upload.MediaUploadHelper
import com.sirelon.aicalories.features.media.upload.MediaUploadUpdate
import com.sirelon.aicalories.features.media.upload.UploadingItem
import com.sirelon.aicalories.supabase.error.RemoteException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
    private val mapper: ReportAnalysisUiMapper,
    private val mediaUploadHelper: MediaUploadHelper,
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
            is AnalyzeContract.AnalyzeEvent.UploadFilesResult -> onFileResult(event)
        }
    }

    private fun onFileResult(event: AnalyzeContract.AnalyzeEvent.UploadFilesResult) {
        viewModelScope.launch {
            mediaUploadHelper
                .uploadSelectedFiles(selectionResult = event.result)
                .catch { error ->
                    showError(error.message ?: "Failed to upload file.")
                }
                .collect(::handleUploadUpdate)
        }
    }

    private fun handleUploadUpdate(update: MediaUploadUpdate) {
        when (update) {
            MediaUploadUpdate.Started -> {
                setState { it.copy(errorMessage = null, hasUploadFailures = false) }
            }

            is MediaUploadUpdate.AddPlaceholder -> {
                addUploadPlaceholder(update.file)
            }

            is MediaUploadUpdate.UploadStarted -> {
                updateUpload(file = update.file) { item ->
                    item.copy(
                        isUploading = true,
                        error = null,
                    )
                }
            }

            is MediaUploadUpdate.Progress -> {
                updateUpload(file = update.file) { item ->
                    item.copy(progress = update.progress)
                }
            }

            is MediaUploadUpdate.Success -> {
                updateUpload(file = update.file) { item ->
                    item.copy(
                        isUploading = false,
                        progress = 100.0,
                        uploadedFile = update.uploadedFile,
                        error = null,
                    )
                }
            }

            is MediaUploadUpdate.Failure -> {
                handleUploadFailure(file = update.file, message = update.message)
            }

            is MediaUploadUpdate.Error -> {
                showError(update.message)
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

            if (uploadedFiles.isEmpty() && (uploadsSnapshot.isNotEmpty() || state.value.hasUploadFailures)) {
                setState { it.copy(isLoading = false) }
                postEffect(AnalyzeContract.AnalyzeEffect.ShowMessage("Upload failed. Please add photos and try again."))
                return@launch
            }

            val note = prompt.ifBlank { null }

            val foodEntryId = repository
                .createFoodEntry(note = note, files = uploadedFiles)
                .onFailure { error ->
                    onError(
                        error = error,
                        fallbackMessage = "Failed to create food entry.",
                    )
                }
                .getOrElse { return@launch }

            foodEntryIdEmitter.emit(foodEntryId)

            repository
                .requestAnalysis(foodEntryId)
                .onFailure { error ->
                    onError(
                        error = error,
                        fallbackMessage = "Failed to start analysis.",
                    )
                }
        }
    }

    private fun onError(error: Throwable, fallbackMessage: String) {
        val isRemote = error is RemoteException
        val message = error.message ?: fallbackMessage

        setState {
            it.copy(
                isLoading = false,
                errorMessage = message.takeUnless { isRemote },
            )
        }

        if (isRemote) {
            postEffect(AnalyzeContract.AnalyzeEffect.ShowMessage(message))
        }
    }

    private fun addUploadPlaceholder(file: KmpFile) {
        setState { current ->
            if (current.uploads.containsKey(file)) {
                current
            } else {
                current.copy(
                    uploads = current.uploads + (file to UploadingItem())
                )
            }
        }
    }

    private fun handleUploadFailure(file: KmpFile, message: String) {
        setState { current ->
            val existing = current.uploads[file] ?: return@setState current
            current.copy(
                uploads = current.uploads + (file to existing.copy(
                    isUploading = false,
                    error = message,
                )),
                hasUploadFailures = true,
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
        reducer: (UploadingItem) -> UploadingItem,
    ) {
        setState { current ->
            val existing = current.uploads[file] ?: return@setState current
            val updatedUploads = current.uploads + (file to reducer(existing))
            current.copy(
                uploads = updatedUploads,
                hasUploadFailures = updatedUploads.values.any { it.error != null },
            )
        }
    }
}
