package com.sirelon.aicalories.features.analyze.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.analyze.common.BaseViewModel
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import kotlinx.coroutines.launch

class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
) : BaseViewModel<AnalyzeContract.AnalyzeState, AnalyzeContract.AnalyzeEvent, AnalyzeContract.AnalyzeEffect>() {

    override fun initialState(): AnalyzeContract.AnalyzeState = AnalyzeContract.AnalyzeState()

    override fun onEvent(event: AnalyzeContract.AnalyzeEvent) {
        when (event) {
            is AnalyzeContract.AnalyzeEvent.PromptChanged -> setState {
                it.copy(
                    prompt = event.value,
                    errorMessage = null
                )
            }

            AnalyzeContract.AnalyzeEvent.Submit -> analyze()
        }
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
