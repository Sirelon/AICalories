package com.sirelon.aicalories.features.history.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.history.ui.HistoryScreenRenderModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
internal class HistoryViewModel() :
    BaseViewModel<HistoryContract.HistoryState, HistoryContract.HistoryEvent, HistoryContract.HistoryEffect>() {

    private val sampleDataProvider: HistorySampleDataProvider = HistorySampleDataProvider

    override fun initialState(): HistoryContract.HistoryState =
        HistoryContract.HistoryState(isLoading = true, renderModel = HistoryScreenRenderModel())

    private val refreshEmitter = MutableStateFlow(1)

    init {
        refreshEmitter
            .onEach {
                setState { it.copy(isLoading = true, errorMessage = null) }

                delay(250) // Simulate work while we still build API integration.
            }
            .flatMapLatest {
                flowOf(sampleDataProvider.randomRenderModel())
            }
            .onEach { renderModel ->
                setState {
                    it.copy(
                        isLoading = false,
                        renderModel = renderModel,
                        errorMessage = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: HistoryContract.HistoryEvent) {
        when (event) {
            HistoryContract.HistoryEvent.Refresh -> refreshEmitter.update {
                it + 1
            }

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
}
