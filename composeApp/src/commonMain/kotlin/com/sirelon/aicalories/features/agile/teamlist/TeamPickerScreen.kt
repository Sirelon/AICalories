package com.sirelon.aicalories.features.agile.teamlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.features.agile.team.Team
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TeamPickerScreen(
    onBack: () -> Unit,
    onTeamSelected: (Int) -> Unit,
    onOpenTeamSettings: (Int) -> Unit,
) {
    val viewModel: TeamPickerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    TeamPickerContent(
        state = state,
        onBack = onBack,
        onTeamSelected = onTeamSelected,
        onOpenTeamSettings = onOpenTeamSettings,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun TeamPickerContent(
    state: TeamPickerContract.TeamPickerState,
    onBack: () -> Unit,
    onTeamSelected: (Int) -> Unit,
    onOpenTeamSettings: (Int) -> Unit,
    onEvent: (TeamPickerContract.TeamPickerEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppLargeAppBar(
                title = "Teams",
                subtitle = "Select or manage a team",
                onBack = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = { onEvent(TeamPickerContract.TeamPickerEvent.AddTeam) },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add team") },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        ) {
            items(
                items = state.teams,
                key = { it.id },
            ) { team ->
                TeamRow(
                    team = team,
                    canRemoveTeam = state.teams.size > 1,
                    onSelect = { onTeamSelected(team.id) },
                    onOpenSettings = { onOpenTeamSettings(team.id) },
                    onRemove = {
                        onEvent(TeamPickerContract.TeamPickerEvent.RemoveTeam(team.id))
                    },
                )
            }
        }
    }
}

@Composable
private fun TeamRow(
    team: Team,
    canRemoveTeam: Boolean,
    onSelect: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "People: ${team.peopleCount}, Capacity: ${team.capacity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                )
            }
            IconButton(onClick = onRemove, enabled = canRemoveTeam) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = if (canRemoveTeam) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun TeamPickerPreview() {
    TeamPickerContent(
        state = TeamPickerContract.TeamPickerState(
            teams = listOf(
                Team(id = 1, name = "Team #1", peopleCount = 5, capacity = 40),
                Team(id = 2, name = "Team #2", peopleCount = 7, capacity = 35),
            )
        ),
        onBack = {},
        onTeamSelected = {},
        onOpenTeamSettings = {},
        onEvent = {},
    )
}
