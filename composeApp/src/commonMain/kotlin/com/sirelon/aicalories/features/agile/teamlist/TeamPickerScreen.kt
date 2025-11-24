package com.sirelon.aicalories.features.agile.teamlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.features.agile.team.Team
import com.sirelon.aicalories.features.agile.team.TeamSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "Teams",
                subtitle = "Select or manage a team",
                onBack = onBack,
                scrollBehavior = scrollBehavior,
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
                .padding(AppDimens.Spacing.xl3)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        ) {
            items(
                items = state.teams,
                key = { it.id },
            ) { team ->
                TeamRow(
                    modifier = Modifier.animateItem(),
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
    modifier: Modifier,
    team: Team,
    canRemoveTeam: Boolean,
    onSelect: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemove: () -> Unit,
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    OutlinedCard(modifier = modifier, onClick = onSelect) {
        SwipeToDismissBox(
            state = swipeToDismissBoxState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = canRemoveTeam,
            onDismiss = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> onOpenSettings()
                    SwipeToDismissBoxValue.EndToStart -> onRemove()
                    SwipeToDismissBoxValue.Settled -> {

                    }
                }

                if (it != SwipeToDismissBoxValue.Settled) {
                    scope.launch {
                        delay(500)
                        swipeToDismissBoxState.reset()
                    }
                }
            },
            backgroundContent = {
                val progress = swipeToDismissBoxState.progress
                when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        SwipeToDismissBackgroundContent(
                            progress = progress,
                            imageVector = Icons.Outlined.Settings,
                            stopColor = AppTheme.colors.primary,
                            alignment = Alignment.CenterStart,
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        SwipeToDismissBackgroundContent(
                            progress = progress,
                            imageVector = Icons.Default.Delete,
                            stopColor = AppTheme.colors.error,
                            alignment = Alignment.CenterEnd,
                        )
                    }

                    SwipeToDismissBoxValue.Settled -> {

                    }
                }
            },
            content = {
                ListItem(
                    headlineContent = {
                        Text(text = team.name)
                    },
                    supportingContent = {
                        TeamSummary(team = team)
                    },
                )
            }
        )
    }
}

@Composable
private fun SwipeToDismissBackgroundContent(
    progress: Float,
    imageVector: ImageVector,
    stopColor: Color,
    alignment: Alignment,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    color = lerp(
                        Color.LightGray,
                        stopColor,
                        progress
                    )
                )
            }
            .wrapContentSize(alignment)
            .padding(AppDimens.Spacing.xl2),
        tint = Color.White
    )
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
