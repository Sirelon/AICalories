package com.sirelon.aicalories.features.datagenerator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.sirelon.aicalories.designsystem.AppCheckboxRow
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.buttons.MagicGreenButton
import com.sirelon.aicalories.features.datagenerator.model.DoubleRange
import com.sirelon.aicalories.features.datagenerator.model.IntRange
import com.sirelon.aicalories.features.datagenerator.presentation.DataGeneratorContract
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.*
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
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
    val randomDataGeneratedMsg = stringResource(Res.string.random_data_generated)
    val allDataClearedMsg = stringResource(Res.string.all_data_cleared)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is DataGeneratorContract.DataGeneratorEffect.DataGenerated -> {
                    if (onBack != null) {
                        onBack()
                    } else {
                        snackbarHostState.showSnackbar(randomDataGeneratedMsg)
                    }
                }

                is DataGeneratorContract.DataGeneratorEffect.DataCleared ->
                    snackbarHostState.showSnackbar(allDataClearedMsg)

                is DataGeneratorContract.DataGeneratorEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    val peopleRangeBounds = state.peoplePerTeamBounds
    val capacityBounds = state.teamCapacityBounds

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = stringResource(Res.string.data_generator_title),
                subtitle = stringResource(Res.string.data_generator_subtitle),
                onBack = onBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        enabled = !state.isGenerating && state.existingTeamsCount > 0,
                        onClick = { onEvent(DataGeneratorContract.DataGeneratorEvent.ResetToEmpty) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = stringResource(Res.string.reset_to_empty)
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
                onEvent = onEvent
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
                        text = stringResource(Res.string.existing_teams_format, state.existingTeamsCount),
                        tint = AppTheme.colors.primary
                    )
                }
            }

            // Clear existing data checkbox
            AppCheckboxRow(
                checked = state.config.clearExistingData,
                text = stringResource(Res.string.clear_existing_data),
                onCheckedChange = {
                    onEvent(DataGeneratorContract.DataGeneratorEvent.ClearExistingDataChanged(it))
                }
            )

            HorizontalDivider()

            // Teams configuration
            Text(
                text = stringResource(Res.string.teams_configuration),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.primary
            )

            SingleValueSelector(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.number_of_teams),
                value = state.config.teamsCount.toDouble().coerceIn(1.0, 20.0),
                bounds = 1.0..20.0,
                step = 1.0,
                onValueChange = { newValue ->
                    onEvent(
                        DataGeneratorContract.DataGeneratorEvent.TeamsCountChanged(
                            newValue.roundToInt().toString()
                        )
                    )
                }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.people_per_team),
                min = state.config.teamPeopleCount.min.toDouble()
                    .coerceIn(peopleRangeBounds.min.toDouble(), peopleRangeBounds.max.toDouble()),
                max = state.config.teamPeopleCount.max.toDouble()
                    .coerceIn(peopleRangeBounds.min.toDouble(), peopleRangeBounds.max.toDouble()),
                bounds = peopleRangeBounds.min.toDouble()..peopleRangeBounds.max.toDouble(),
                step = 1.0,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.roundToInt()
                            .coerceIn(peopleRangeBounds.min, peopleRangeBounds.max)
                        val clampedMax = newMax.roundToInt()
                            .coerceIn(peopleRangeBounds.min, peopleRangeBounds.max)
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
                label = stringResource(Res.string.team_capacity_label),
                min = state.config.teamCapacity.min.toDouble()
                    .coerceIn(capacityBounds.min.toDouble(), capacityBounds.max.toDouble()),
                max = state.config.teamCapacity.max.toDouble()
                    .coerceIn(capacityBounds.min.toDouble(), capacityBounds.max.toDouble()),
                bounds = capacityBounds.min.toDouble()..capacityBounds.max.toDouble(),
                step = 1.0,
                allowNull = false,
                onRangeChange = { newMin, newMax ->
                    if (newMin != null && newMax != null) {
                        val clampedMin = newMin.roundToInt()
                            .coerceIn(capacityBounds.min, capacityBounds.max)
                        val clampedMax = newMax.roundToInt()
                            .coerceIn(capacityBounds.min, capacityBounds.max)
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
                label = stringResource(Res.string.risk_factor),
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
                text = stringResource(Res.string.stories_configuration),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.primary
            )

            SingleValueSelector(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.stories_per_team),
                value = state.config.storiesPerTeamCount.toDouble().coerceIn(1.0, 20.0),
                bounds = 1.0..20.0,
                step = 1.0,
                onValueChange = { newValue ->
                    onEvent(
                        DataGeneratorContract.DataGeneratorEvent.StoriesPerTeamChanged(
                            newValue.roundToInt().toString()
                        )
                    )
                }
            )

            RangeSelector(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.tickets_per_story),
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
    onEvent: (DataGeneratorContract.DataGeneratorEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.Spacing.xl3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        MagicGreenButton(
            modifier = Modifier.fillMaxWidth().height(AppDimens.Size.xl12),
            text = stringResource(Res.string.generate_random_data),
            enabled = !state.isGenerating,
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
