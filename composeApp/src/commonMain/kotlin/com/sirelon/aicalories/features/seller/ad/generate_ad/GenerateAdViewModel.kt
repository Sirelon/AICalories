package com.sirelon.aicalories.features.seller.ad.generate_ad

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.media.upload.MediaUploadHelper
import com.sirelon.aicalories.features.media.upload.MediaUploadUpdate
import com.sirelon.aicalories.features.media.upload.UploadingItem
import com.sirelon.aicalories.features.seller.ad.AdvertisementWithAttributes
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.openai.OpenAIClient
import com.sirelon.aicalories.supabase.error.RemoteException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class GenerateAdViewModel(
    private val mediaUploadHelper: MediaUploadHelper,
    private val categoriesRepository: CategoriesRepository,
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
                    submit()
                }
            }

            is GenerateAdContract.GenerateAdEvent.UploadFilesResult -> onFileResult(event)

            is GenerateAdContract.GenerateAdEvent.RemovePhoto -> {
                setState { current -> current.copy(uploads = current.uploads - event.file) }
            }
        }
    }

    private suspend fun submit() {
        flowOf(1)
            .onStart {
                setState { it.copy(isLoading = true, completedSteps = 0, errorMessage = null) }
            }

            .map { uploadFilesAndGetPublicUrls() }
            .onEach { setState { it.copy(completedSteps = 1) } }

            .catch { error ->
                setState { it.copy(isLoading = false) }
                showError(error.message ?: "Upload failed")
            }
            .map { openAi.analyzeThing(images = it, sellerPrompt = state.value.prompt) }
            .onEach { setState { it.copy(completedSteps = 2) } }

            // get category, attributes, so on
            .flatMapLatest { data ->
                categoriesRepository
                    .categorySuggestion(data.second.title)
                    .onEach { setState { it.copy(completedSteps = 3) } }

                    .flatMapLatest { categoriesRepository.getAttributes(it.id) }
                    .onEach { setState { it.copy(completedSteps = 4) } }
                    // load openAi for fill attributes
                    .map {
                        openAi.fillAdditionalInfo(
                            previousResponseId = data.first,
                            attributes = it,
                            sellerPrompt = state.value.prompt
                        )
                    }
                    .onEach { setState { it.copy(completedSteps = 5) } }
                    .map {
                        AdvertisementWithAttributes(
                            advertisement = data.second,
                            filledAttributes = it
                        )
                    }
            }

            .onEach {
                postEffect(GenerateAdContract.GenerateAdEffect.OpenAdPreview(it))
            }
            .catch { error ->
                setState { it.copy(isLoading = false, errorMessage = error.message) }
            }
            .onCompletion {
                setState { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun uploadFilesAndGetPublicUrls(): List<String> {
        val pendingFiles = currentState()
            .uploads
            .filter { (_, item) -> item.isPending }
            .keys.toList()

        val uploadedUrls = mediaUploadHelper
            .uploadPreparedFiles(pendingFiles)
            .onEach(::handleUploadUpdate)
            .filterIsInstance<MediaUploadUpdate.Success>()
            .map { it.uploadedFile }
            .toList()
            .map { mediaUploadHelper.publicUrl(it.path) }
        return uploadedUrls
    }

    private fun onFileResult(event: GenerateAdContract.GenerateAdEvent.UploadFilesResult) {
        viewModelScope.launch {
            mediaUploadHelper
                .prepareFiles(selectionResult = event.result)
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
