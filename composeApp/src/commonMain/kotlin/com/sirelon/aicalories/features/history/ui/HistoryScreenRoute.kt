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
    viewModel: HistoryViewModel = koinViewModel(),
    onBack: (() -> Unit)? = null,
    onEntrySelected: (Long) -> Unit = {},
    onCaptureNewMeal: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(HistoryContract.HistoryEvent.ScreenShown)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryContract.HistoryEffect.OpenEntryDetails -> onEntrySelected(effect.entryId)
                HistoryContract.HistoryEffect.RequestCaptureNewMeal -> onCaptureNewMeal()
            }
        }
    }

    val renderModel = state.renderModel ?: HistoryScreenRenderModel(
        header = HistoryHeaderRenderModel(
            title = "History & Insights",
            subtitle = "Track your analysed meals",
        ),
        emptyState = HistoryEmptyStateRenderModel(
            title = if (state.isLoading) "Loading history…" else "No history yet",
            description = if (state.isLoading) {
                "Preparing sample data..."
            } else {
                "Analyze your first meal to see items here."
            },
            actionLabel = "Capture meal",
        ),
    )

    HistoryScreen(
        renderModel = renderModel,
        onBack = onBack,
        onEntryClick = { entry ->
            viewModel.onEvent(HistoryContract.HistoryEvent.EntryClicked(entry.id))
        },
        onEmptyStateAction = {
            viewModel.onEvent(HistoryContract.HistoryEvent.EmptyCtaClicked)
        },
    )
}
