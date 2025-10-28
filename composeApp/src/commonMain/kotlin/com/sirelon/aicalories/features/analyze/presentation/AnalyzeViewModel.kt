package com.sirelon.aicalories.features.analyze.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.analyze.data.AnalyzeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyzeViewModel(
    private val repository: AnalyzeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyzeContract.AnalyzeState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AnalyzeContract.AnalyzeEffect>()
    val effects = _effects.asSharedFlow()

    fun onEvent(event: AnalyzeContract.AnalyzeEvent) {
        when (event) {
            is AnalyzeContract.AnalyzeEvent.PromptChanged -> _state.update {
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
            viewModelScope.launch {
                _effects.emit(AnalyzeContract.AnalyzeEffect.ShowMessage("Describe your meal first."))
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            repository
                .analyzeDescription(prompt)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            prompt = "",
                            isLoading = false,
                            result = result,
                        )
                    }
                    _effects.emit(AnalyzeContract.AnalyzeEffect.ShowMessage("Analysis prepared."))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to analyze this meal.",
                        )
                    }
                }
        }
    }
}
