package com.sirelon.aicalories.features.agile.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.templates.AppExpandableCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TeamScreen(
    onBack: () -> Unit,
    teamId: Int,
) {
    val viewModel: TeamViewModel = koinViewModel(parameters = { parametersOf(teamId) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    TeamScreenContent(
        state = state,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun TeamScreenContent(
    state: TeamContract.TeamState,
    onBack: () -> Unit,
    onEvent: (TeamContract.TeamEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val team = state.team
    val totalCapacity = team.capacity
    val totalPeople = team.peopleCount

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "Team settings",
                subtitle = "Team size & capacity",
                onBack = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
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
                            text = "People: ${team.peopleCount} | Capacity: ${team.capacity}",
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
            value = team.peopleCount.toString(),
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
            value = team.capacity.toString(),
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
}
