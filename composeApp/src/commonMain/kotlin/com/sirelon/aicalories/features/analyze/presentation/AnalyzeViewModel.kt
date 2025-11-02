package com.sirelon.aicalories.features.analyze.presentation

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.features.analyze.common.BaseViewModel
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
) : BaseViewModel<AnalyzeContract.AnalyzeState, AnalyzeContract.AnalyzeEvent, AnalyzeContract.AnalyzeEffect>() {

    override fun initialState(): AnalyzeContract.AnalyzeState = AnalyzeContract.AnalyzeState()

    val images = mutableStateMapOf<KmpFile, Double>()

    val uploadImagesFlow = MutableStateFlow<KmpFile?>(null)

    // TODO:
    lateinit var platformContext: PlatformContext

    init {
        uploadImagesFlow
            .filterNotNull()
            .flatMapLatest(::uploadFileFlow)
            .catch { it.printStackTrace() }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: AnalyzeContract.AnalyzeEvent) {
        when (event) {
            is AnalyzeContract.AnalyzeEvent.PromptChanged -> setState {
                it.copy(
                    prompt = event.value,
                    errorMessage = null
                )
            }

            AnalyzeContract.AnalyzeEvent.Submit -> analyze()
            is AnalyzeContract.AnalyzeEvent.UploadFilesResult -> viewModelScope.launch {
                event
                    .result
                    .onSuccess {
                        it
                            .map { async { uploadImagesFlow.emit(it) } }
                            .awaitAll()
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

                is UploadStatus.Success -> 1.0
            }

            images[file] = percent
        }

    private fun analyze() {
        val prompt = state.value.prompt.trim()
        if (prompt.isEmpty()) {
            postEffect(effect = AnalyzeContract.AnalyzeEffect.ShowMessage("Describe your meal first."))
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
                .analyzeDescription(prompt)
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
