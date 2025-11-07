package com.sirelon.aicalories.features.history.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val sampleDataProvider: HistorySampleDataProvider = HistorySampleDataProvider,
) : BaseViewModel<HistoryContract.HistoryState, HistoryContract.HistoryEvent, HistoryContract.HistoryEffect>() {

    override fun initialState(): HistoryContract.HistoryState = HistoryContract.HistoryState(isLoading = true)

    init {
        loadHistory(force = true)
    }

    override fun onEvent(event: HistoryContract.HistoryEvent) {
        when (event) {
            HistoryContract.HistoryEvent.ScreenShown -> {
                if (state.value.renderModel == null && !state.value.isLoading) {
                    loadHistory()
                }
            }

            HistoryContract.HistoryEvent.Refresh -> loadHistory(force = true)
            is HistoryContract.HistoryEvent.EntryClicked -> {
                postEffect(HistoryContract.HistoryEffect.OpenEntryDetails(event.entryId))
            }

            HistoryContract.HistoryEvent.EmptyCtaClicked -> {
                postEffect(HistoryContract.HistoryEffect.RequestCaptureNewMeal)
            }

            HistoryContract.HistoryEvent.ErrorConsumed -> {
                setState { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadHistory(force: Boolean = false) {
        if (state.value.isLoading && !force) return

        viewModelScope.launch {
            setState { it.copy(isLoading = true, errorMessage = null) }
            delay(250) // Simulate work while we still build API integration.
            val renderModel = sampleDataProvider.randomRenderModel()
            setState {
                it.copy(
                    isLoading = false,
                    renderModel = renderModel,
                    errorMessage = null,
                )
            }
        }
    }
}
