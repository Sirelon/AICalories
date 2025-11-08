package com.sirelon.aicalories.features.history.presentation

import com.sirelon.aicalories.features.history.ui.HistoryScreenRenderModel

interface HistoryContract {

    data class HistoryState(
        val isLoading: Boolean = false,
        val renderModel: HistoryScreenRenderModel,
        val errorMessage: String? = null,
    )

    sealed interface HistoryEvent {
        data object Refresh : HistoryEvent
        data class EntryClicked(val entryId: Long) : HistoryEvent
        data object EmptyCtaClicked : HistoryEvent
        data object ErrorConsumed : HistoryEvent
    }

    sealed interface HistoryEffect {
        data class OpenEntryDetails(val entryId: Long) : HistoryEffect
        data object RequestCaptureNewMeal : HistoryEffect
    }
}
