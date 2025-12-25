package com.sirelon.aicalories.features.datagenerator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import com.sirelon.aicalories.designsystem.AppCheckboxRow
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.buttons.MagicBlueButton
import com.sirelon.aicalories.designsystem.buttons.MagicGreenButton
import com.sirelon.aicalories.features.datagenerator.model.DoubleRange
import com.sirelon.aicalories.features.datagenerator.model.IntRange
import com.sirelon.aicalories.features.datagenerator.presentation.DataGeneratorContract
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DataGeneratorScreenContent(
    state: DataGeneratorContract.DataGeneratorState,
    effects: Flow<DataGeneratorContract.DataGeneratorEffect>,
    onEvent: (DataGeneratorContract.DataGeneratorEvent) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is DataGeneratorContract.DataGeneratorEffect.DataGenerated ->
                    snackbarHostState.showSnackbar("Random data generated successfully!")

                is DataGeneratorContract.DataGeneratorEffect.DataCleared ->
                    snackbarHostState.showSnackbar("All data cleared!")

                is DataGeneratorContract.DataGeneratorEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "Data Generator",
                subtitle = "Generate random agile test data",
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Action buttons
            ActionButtons(state, onEvent)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2)
        ) {
            // Existing data info
            if (state.existingTeamsCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.surface
                    )
                ) {
                    TextWithIcon(
                        modifier = Modifier.padding(AppDimens.Spacing.xl2),
                        icon = Icons.Default.Info,
                        text = "Existing teams: ${state.existingTeamsCount}",
                        tint = AppTheme.colors.primary
                    )
                }
            }

            // Clear existing data checkbox
            AppCheckboxRow(
                checked = state.config.clearExistingData,
                text = "Clear existing data before generating",
                onCheckedChange = {
                    onEvent(DataGeneratorContract.DataGeneratorEvent.ClearExistingDataChanged(it))
                }
            )

            HorizontalDivider()

            // Teams configuration
            Text(
                text = "Teams Configuration",
                style = AppTheme.typography.headline,
                color = AppTheme.colors.primary
            )

            NumberedInput(
                modifier = Modifier.fillMaxWidth(),
                value = state.config.teamsCount.toString(),
                onValueChange = {
                    onEvent(
                        DataGeneratorContract.DataGeneratorEvent.TeamsCountChanged(
                            it
                        )
                    )
                },
                label = "Number of Teams"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
            ) {
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamPeopleCount.min.toString(),
                    onValueChange = { value ->
                        updateIntRangeMin(state.config.teamPeopleCount, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamPeopleCountRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Min People"
                )
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamPeopleCount.max.toString(),
                    onValueChange = { value ->
                        updateIntRangeMax(state.config.teamPeopleCount, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamPeopleCountRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Max People"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
            ) {
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamCapacity.min.toString(),
                    onValueChange = { value ->
                        updateIntRangeMin(state.config.teamCapacity, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamCapacityRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Min Capacity"
                )
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamCapacity.max.toString(),
                    onValueChange = { value ->
                        updateIntRangeMax(state.config.teamCapacity, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamCapacityRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Max Capacity"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
            ) {
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamRiskFactor.min.toString(),
                    onValueChange = { value ->
                        updateDoubleRangeMin(state.config.teamRiskFactor, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamRiskFactorRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Min Risk (0.0-1.0)",
                    keyboardType = KeyboardType.Decimal
                )
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.teamRiskFactor.max.toString(),
                    onValueChange = { value ->
                        updateDoubleRangeMax(state.config.teamRiskFactor, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TeamRiskFactorRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Max Risk (0.0-1.0)",
                    keyboardType = KeyboardType.Decimal
                )
            }

            HorizontalDivider()

            // Stories configuration
            Text(
                text = "Stories Configuration",
                style = AppTheme.typography.headline,
                color = AppTheme.colors.primary
            )

            NumberedInput(
                modifier = Modifier.fillMaxWidth(),
                value = state.config.storiesPerTeamCount.toString(),
                onValueChange = {
                    onEvent(
                        DataGeneratorContract.DataGeneratorEvent.StoriesPerTeamChanged(
                            it
                        )
                    )
                },
                label = "Stories per Team"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
            ) {
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.ticketsPerStory.min.toString(),
                    onValueChange = { value ->
                        updateIntRangeMin(state.config.ticketsPerStory, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TicketsPerStoryRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Min Tickets per Story"
                )
                NumberedInput(
                    modifier = Modifier.weight(1f),
                    value = state.config.ticketsPerStory.max.toString(),
                    onValueChange = { value ->
                        updateIntRangeMax(state.config.ticketsPerStory, value)
                            ?.let {
                                onEvent(
                                    DataGeneratorContract.DataGeneratorEvent.TicketsPerStoryRangeChanged(
                                        it
                                    )
                                )
                            }
                    },
                    label = "Max Tickets per Story"
                )
            }

            HorizontalDivider()

            if (state.isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    state: DataGeneratorContract.DataGeneratorState,
    onEvent: (DataGeneratorContract.DataGeneratorEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.Spacing.xl3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        MagicGreenButton(
            modifier = Modifier.fillMaxWidth().height(AppDimens.Size.xl12),
            text = "Generate Random Data",
            enabled = !state.isGenerating,
            onClick = { onEvent(DataGeneratorContract.DataGeneratorEvent.GenerateRandomData) }
        )

        MagicBlueButton(
            modifier = Modifier.fillMaxWidth().height(AppDimens.Size.xl12),
            text = "Reset to Empty",
            enabled = !state.isGenerating && state.existingTeamsCount > 0,
            onClick = { onEvent(DataGeneratorContract.DataGeneratorEvent.ResetToEmpty) }
        )
    }
}

@Composable
private fun TextWithIcon(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Text(
            text = text,
            style = AppTheme.typography.body
        )
    }
}

@Composable
private fun NumberedInput(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
) {
    Input(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

private fun updateIntRangeMin(current: IntRange, newMinText: String): IntRange? {
    val newMin = newMinText.toIntOrNull() ?: return null
    return if (newMin <= current.max) current.copy(min = newMin) else null
}

private fun updateIntRangeMax(current: IntRange, newMaxText: String): IntRange? {
    val newMax = newMaxText.toIntOrNull() ?: return null
    return if (newMax >= current.min) current.copy(max = newMax) else null
}

private fun updateDoubleRangeMin(current: DoubleRange, newMinText: String): DoubleRange? {
    val newMin = newMinText.toDoubleOrNull() ?: return null
    return if (newMin <= current.max) current.copy(min = newMin) else null
}

private fun updateDoubleRangeMax(current: DoubleRange, newMaxText: String): DoubleRange? {
    val newMax = newMaxText.toDoubleOrNull() ?: return null
    return if (newMax >= current.min) current.copy(max = newMax) else null
}
