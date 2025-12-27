package com.sirelon.aicalories.features.agile.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import com.sirelon.aicalories.designsystem.AppScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppSectionHeader
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.templates.AppExpandableCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun TeamScreen(
    teamId: Int,
) {
    val viewModel: TeamViewModel = koinViewModel(
        key = "team_settings_$teamId",
        parameters = { parametersOf(teamId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    TeamScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun TeamScreenContent(
    state: TeamContract.TeamState,
    onEvent: (TeamContract.TeamEvent) -> Unit,
) {
    val team = state.team
    val riskPercentage = (team.riskFactor.coerceIn(0.0, 1.0) * 100).roundToInt()

    AppScaffold(
        modifier = Modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = AppDimens.Spacing.xl3, vertical = AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            AppSectionHeader(
                title = "Team settings",
                subtitle = "Team size & capacity",
            )
            TeamSummary(team = team)

            AppExpandableCard(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = AppDimens.Spacing.m),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "People: ${team.peopleCount} | Capacity: ${team.capacity} | Risk: $riskPercentage%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            ) {
                TeamFields(
                    team = team,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun TeamFields(
    team: Team,
    onEvent: (TeamContract.TeamEvent) -> Unit,
) {
    Input(
        modifier = Modifier.fillMaxWidth(),
        value = team.name,
        onValueChange = {
            onEvent(TeamContract.TeamEvent.NameChanged(teamId = team.id, name = it))
        },
        singleLine = true,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Input(
            modifier = Modifier.weight(1f),
            value = team.peopleCount.takeIf { it > 0 }?.toString().orEmpty(),
            onValueChange = {
                onEvent(
                    TeamContract.TeamEvent.PeopleCountChanged(
                        teamId = team.id,
                        peopleCount = it,
                    )
                )
            },
            label = "People",
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        )
        Input(
            modifier = Modifier.weight(1f),
            value = team.capacity.takeIf { it > 0 }?.toString().orEmpty(),
            onValueChange = {
                onEvent(
                    TeamContract.TeamEvent.CapacityChanged(
                        teamId = team.id,
                        capacity = it,
                    )
                )
            },
            label = "Capacity",
            supportingText = "Story points per sprint",
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        )
    }
    Input(
        modifier = Modifier.fillMaxWidth(),
        value = team.riskFactor.takeIf { it > 0 }?.toString().orEmpty(),
        onValueChange = {
            onEvent(
                TeamContract.TeamEvent.RiskFactorChanged(
                    teamId = team.id,
                    riskFactor = it,
                )
            )
        },
        label = "Risk factor",
        supportingText = "0.0 to 1.0 (20% risk = 0.2)",
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
    )
}
