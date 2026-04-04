package com.sirelon.aicalories.features.seller.ad.generate_ad

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.media.upload.MediaUploadHelper
import com.sirelon.aicalories.features.media.upload.MediaUploadUpdate
import com.sirelon.aicalories.features.media.upload.UploadingItem
import com.sirelon.aicalories.network.OpenAIClient
import com.sirelon.aicalories.supabase.error.RemoteException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GenerateAdViewModel(
    private val mediaUploadHelper: MediaUploadHelper,
    private val openAi: OpenAIClient,
) : BaseViewModel<GenerateAdContract.GenerateAdState, GenerateAdContract.GenerateAdEvent, GenerateAdContract.GenerateAdEffect>() {

    override fun initialState(): GenerateAdContract.GenerateAdState =
        GenerateAdContract.GenerateAdState()

    override fun onEvent(event: GenerateAdContract.GenerateAdEvent) {
        when (event) {
            is GenerateAdContract.GenerateAdEvent.PromptChanged -> {
                setState {
                    it.copy(
                        prompt = event.value,
                        errorMessage = null,
                    )
                }
            }

            is GenerateAdContract.GenerateAdEvent.Submit -> {
                viewModelScope.launch {
                    setState { it.copy(isLoading = true, errorMessage = null) }

                    val platformContext = event.platformContext
                    val pendingFiles = state.value.uploads
                        .filter { (_, item) -> item.isPending }
                        .keys.toList()

                    var uploadFailed = false

                    if (pendingFiles.isNotEmpty()) {
                        mediaUploadHelper
                            .uploadPreparedFiles(platformContext, pendingFiles)
                            .catch { error ->
                                uploadFailed = true
                                setState { it.copy(isLoading = false) }
                                showError(error.message ?: "Upload failed")
                            }
                            .collect { update ->
                                handleUploadUpdate(update)
                                when (update) {
                                    is MediaUploadUpdate.Failure,
                                    is MediaUploadUpdate.Error -> uploadFailed = true

                                    else -> {}
                                }
                            }
                    }

                    if (uploadFailed) {
                        setState { it.copy(isLoading = false) }
                        return@launch
                    }

                    val uploadedUrls = state.value.uploads
                        .filter { (_, item) -> item.uploadedFile != null }
                        .mapNotNull { (_, item) ->
                            item.uploadedFile?.path?.let {
                                mediaUploadHelper.publicUrl(
                                    it
                                )
                            }
                        }

                    runCatching {
                        val result = openAi.analyzeThing(uploadedUrls)
                        setState { it.copy(isLoading = false) }
                        postEffect(GenerateAdContract.GenerateAdEffect.OpenAdPreview(result))
                    }.onFailure { error ->
                        setState { it.copy(isLoading = false, errorMessage = error.message) }
                    }
                }
            }

            is GenerateAdContract.GenerateAdEvent.UploadFilesResult -> onFileResult(event)
        }
    }

    private fun onFileResult(event: GenerateAdContract.GenerateAdEvent.UploadFilesResult) {
        viewModelScope.launch {
            mediaUploadHelper
                .prepareFiles(
                    platformContext = event.platformContext,
                    selectionResult = event.result,
                )
                .onSuccess { files ->
                    setState { current ->
                        val newEntries = files
                            .filter { file -> !current.uploads.containsKey(file) }
                            .associateWith { UploadingItem() }
                        current.copy(uploads = current.uploads + newEntries)
                    }
                }
                .onFailure { error ->
                    showError(error.message ?: "Failed to process selected files.")
                }
        }
    }

    private fun handleUploadUpdate(update: MediaUploadUpdate) {
        when (update) {
            MediaUploadUpdate.Started -> {
                setState { it.copy(errorMessage = null) }
            }

            is MediaUploadUpdate.AddPlaceholder -> {
                addUploadPlaceholder(update.file)
            }

            is MediaUploadUpdate.UploadStarted -> {
                updateUpload(file = update.file) { item ->
                    item.copy(isUploading = true)
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
                        progress = 100.0,
                        uploadedFile = update.uploadedFile,
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
            postEffect(GenerateAdContract.GenerateAdEffect.ShowMessage(message))
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
            current.copy(
                errorMessage = message,
                uploads = current.uploads - file,
            )
        }
        postEffect(GenerateAdContract.GenerateAdEffect.ShowMessage(message))
    }

    private fun showError(message: String) {
        setState { it.copy(errorMessage = message) }
        postEffect(GenerateAdContract.GenerateAdEffect.ShowMessage(message))
    }

    private fun updateUpload(
        file: KmpFile,
        reducer: (UploadingItem) -> UploadingItem,
    ) {
        setState { current ->
            val existing = current.uploads[file] ?: return@setState current
            current.copy(
                uploads = current.uploads + (file to reducer(existing))
            )
        }
    }
}