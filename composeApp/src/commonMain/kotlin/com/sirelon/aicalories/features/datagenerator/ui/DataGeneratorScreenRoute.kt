package com.sirelon.aicalories.features.datagenerator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.features.datagenerator.presentation.DataGeneratorViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DataGeneratorScreen(onBack: (() -> Unit)? = null) {
    val viewModel: DataGeneratorViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    DataGeneratorScreenContent(
        state = state,
        effects = viewModel.effects,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}
