package com.sirelon.aicalories.features.history.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.features.history.presentation.HistoryContract
import com.sirelon.aicalories.features.history.presentation.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreenRoute(
    onBack: (() -> Unit)? = null,
    onEntrySelected: (Long) -> Unit = {},
    onCaptureNewMeal: () -> Unit = {},
) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryContract.HistoryEffect.OpenEntryDetails -> onEntrySelected(effect.entryId)
                HistoryContract.HistoryEffect.RequestCaptureNewMeal -> onCaptureNewMeal()
            }
        }
    }

    HistoryScreen(
        renderModel = state.renderModel,
        isLoading = state.isLoading,
        onBack = onBack,
        onEntryClick = { entry ->
            viewModel.onEvent(HistoryContract.HistoryEvent.EntryClicked(entry.id))
        },
        onEmptyStateAction = {
            viewModel.onEvent(HistoryContract.HistoryEvent.EmptyCtaClicked)
        },
    )
}
