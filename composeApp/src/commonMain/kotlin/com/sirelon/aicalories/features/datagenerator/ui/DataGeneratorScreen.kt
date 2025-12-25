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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.sirelon.aicalories.designsystem.buttons.MagicGreenButton
import com.sirelon.aicalories.features.datagenerator.model.DoubleRange
import com.sirelon.aicalories.features.datagenerator.model.IntRange
import com.sirelon.aicalories.features.datagenerator.presentation.DataGeneratorContract
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

@Composable
internal fun DataGeneratorScreenContent(
    state: DataGeneratorContract.DataGeneratorState,
    effects: Flow<DataGeneratorContract.DataGeneratorEffect>,
    onEvent: (DataGeneratorContract.DataGeneratorEvent) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val blankInputs = remember { mutableStateMapOf<String, Boolean>() }
    val hasBlankInput by remember { derivedStateOf { blankInputs.values.any { it } } }
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is DataGeneratorContract.DataGeneratorEffect.DataGenerated -> {
                    if (onBack != null) {
                        onBack()
                    } else {
                        snackbarHostState.showSnackbar("Random data generated successfully!")
                    }
                }

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
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        enabled = !state.isGenerating && state.existingTeamsCount > 0,
                        onClick = { onEvent(DataGeneratorContract.DataGeneratorEvent.ResetToEmpty) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset to Empty"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Action buttons
            ActionButtons(
                state = state,
                onEvent = onEvent,
                hasBlankInput = hasBlankInput
            )
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
                label = "Number of Teams",
                onBlankChanged = { blankInputs["teamsCount"] = it }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "People per Team",
                min = state.config.teamPeopleCount.min.toDouble().coerceIn(1.0, 50.0),
                max = state.config.teamPeopleCount.max.toDouble().coerceIn(1.0, 50.0),
                bounds = 1.0..50.0,
                step = 1.0,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.roundToInt().coerceIn(1, 50)
                        val clampedMax = newMax.roundToInt().coerceIn(1, 50)
                        onEvent(
                            DataGeneratorContract.DataGeneratorEvent.TeamPeopleCountRangeChanged(
                                IntRange(clampedMin, clampedMax)
                            )
                        )
                    }
                }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "Team Capacity",
                min = state.config.teamCapacity.min.toDouble().coerceIn(1.0, 100.0),
                max = state.config.teamCapacity.max.toDouble().coerceIn(1.0, 100.0),
                bounds = 1.0..100.0,
                step = 1.0,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.roundToInt().coerceIn(1, 100)
                        val clampedMax = newMax.roundToInt().coerceIn(1, 100)
                        onEvent(
                            DataGeneratorContract.DataGeneratorEvent.TeamCapacityRangeChanged(
                                IntRange(clampedMin, clampedMax)
                            )
                        )
                    }
                }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "Risk Factor",
                min = state.config.teamRiskFactor.min.coerceIn(0.0, 1.0),
                max = state.config.teamRiskFactor.max.coerceIn(0.0, 1.0),
                bounds = 0.0..1.0,
                step = 0.01,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.coerceIn(0.0, 1.0)
                        val clampedMax = newMax.coerceIn(0.0, 1.0)
                        onEvent(
                            DataGeneratorContract.DataGeneratorEvent.TeamRiskFactorRangeChanged(
                                DoubleRange(clampedMin, clampedMax)
                            )
                        )
                    }
                }
            )

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
                label = "Stories per Team",
                onBlankChanged = { blankInputs["storiesPerTeam"] = it }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "Tickets per Story",
                min = state.config.ticketsPerStory.min.toDouble().coerceIn(1.0, 50.0),
                max = state.config.ticketsPerStory.max.toDouble().coerceIn(1.0, 50.0),
                bounds = 1.0..50.0,
                step = 1.0,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.roundToInt().coerceIn(1, 50)
                        val clampedMax = newMax.roundToInt().coerceIn(1, 50)
                        onEvent(
                            DataGeneratorContract.DataGeneratorEvent.TicketsPerStoryRangeChanged(
                                IntRange(clampedMin, clampedMax)
                            )
                        )
                    }
                }
            )

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
    onEvent: (DataGeneratorContract.DataGeneratorEvent) -> Unit,
    hasBlankInput: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.Spacing.xl3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        MagicGreenButton(
            modifier = Modifier.fillMaxWidth().height(AppDimens.Size.xl12),
            text = "Generate Random Data",
            enabled = !state.isGenerating && !hasBlankInput,
            onClick = { onEvent(DataGeneratorContract.DataGeneratorEvent.GenerateRandomData) }
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
    onBlankChanged: (Boolean) -> Unit = {},
) {
    var text by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        text = value
    }

    LaunchedEffect(text) {
        onBlankChanged(text.isBlank())
    }

    Input(
        modifier = modifier,
        value = text,
        onValueChange = { newValue ->
            text = newValue
            if (newValue.isNotBlank()) {
                onValueChange(newValue)
            }
        },
        label = label,
        supportingText = if (text.isBlank()) "Required" else null,
        isError = text.isBlank(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
