package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.templates.AppExpandableCard
import com.sirelon.aicalories.features.agile.model.Ticket
import com.sirelon.aicalories.features.agile.model.UserStory
import com.sirelon.aicalories.features.agile.presentation.AgileContract
import com.sirelon.aicalories.features.agile.presentation.AgileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgileScreen(
    onBack: () -> Unit,
) {
    val viewModel: AgileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AgileScreenContent(
        state = state,
        onBack = onBack,
        onAddUserStory = { viewModel.onEvent(AgileContract.AgileEvent.AddUserStory) },
        onAddTicket = { storyId ->
            viewModel.onEvent(AgileContract.AgileEvent.AddTicket(storyId))
        },
        onStoryNameChange = { storyId, name ->
            viewModel.onEvent(AgileContract.AgileEvent.StoryNameChanged(storyId, name))
        },
        onTicketNameChange = { storyId, ticketId, name ->
            viewModel.onEvent(AgileContract.AgileEvent.TicketNameChanged(storyId, ticketId, name))
        },
        onTicketEstimationChange = { storyId, ticketId, estimation ->
            viewModel.onEvent(
                AgileContract.AgileEvent.TicketEstimationChanged(
                    storyId = storyId,
                    ticketId = ticketId,
                    estimation = estimation,
                )
            )
        },
    )
}

@Composable
private fun AgileScreenContent(
    state: AgileContract.AgileState,
    onBack: () -> Unit,
    onAddUserStory: () -> Unit,
    onAddTicket: (Int) -> Unit,
    onStoryNameChange: (Int, String) -> Unit,
    onTicketNameChange: (Int, Int, String) -> Unit,
    onTicketEstimationChange: (Int, Int, Estimation) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val listState = rememberLazyListState()

    // FAB expands only near the top
    val fabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "Agile",
                onBack = onBack,
                subtitle = "Calculation logic",
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddUserStory,
                text = { Text("Add User story") },
                icon = {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                },
                expanded = fabExpanded,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            state = listState,
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(AppDimens.Spacing.xl3),
                    text = "Count of user stories: ${state.stories.size}",
                )
            }

            items(
                items = state.stories,
                key = { story -> story.id },
            ) { story ->

                AppExpandableCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = AppDimens.Spacing.m),
                            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
                        ) {
                            Input(
                                modifier = Modifier.fillMaxWidth(),
                                value = story.name,
                                onValueChange = { onStoryNameChange(story.id, it) },
                                singleLine = true,
                            )
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.alpha(0.85f),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
                    ) {
                        story.tickets.forEach { ticket ->
                            TicketInput(
                                ticket = ticket,
                                onTicketNameChange = { onTicketNameChange(story.id, ticket.id, it) },
                                onTicketEstimationChange = { estimationValue ->
                                    onTicketEstimationChange(story.id, ticket.id, estimationValue)
                                },
                            )
                        }
                        TextButton(
                            onClick = { onAddTicket(story.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = AppDimens.Spacing.m),
                                text = "Add ticket",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketInput(
    ticket: Ticket,
    onTicketNameChange: (String) -> Unit,
    onTicketEstimationChange: (Estimation) -> Unit,
) {
    var isSheetOpen by remember { mutableStateOf(false) }

    Input(
        modifier = Modifier.fillMaxWidth(),
        value = ticket.name,
        onValueChange = onTicketNameChange,
        singleLine = true,
        trailingIcon = {
            TicketEstimationTrailing(
                estimation = ticket.estimation,
                onClick = { isSheetOpen = true },
            )
        },
    )

    if (isSheetOpen) {
        EstimationPickerSheet(
            selected = ticket.estimation,
            onSelected = {
                onTicketEstimationChange(it)
                isSheetOpen = false
            },
            onDismissRequest = { isSheetOpen = false },
        )
    }
}

@Composable
private fun TicketEstimationTrailing(
    estimation: Estimation,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 0.dp,
        color = estimation.color().copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppDimens.Spacing.l,
                vertical = AppDimens.Spacing.s,
            ),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = estimation.code(),
                style = MaterialTheme.typography.labelLarge,
                color = estimation.color(),
            )
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun AgileScreenPreview() {
    AgileScreenContent(
        state = AgileContract.AgileState(
            stories = listOf(
                UserStory(
                    id = 1,
                    name = "User Story #1",
                    tickets = listOf(
                        Ticket(id = 1, name = "Ticket #1"),
                        Ticket(id = 2, name = "Ticket #2", estimation = Estimation.L),
                    ),
                ),
                UserStory(
                    id = 2,
                    name = "User Story #2",
                    tickets = emptyList(),
                ),
            ),
            nextStoryId = 3,
            nextTicketId = 3,
        ),
        onBack = {},
        onAddUserStory = {},
        onAddTicket = { _ -> },
        onStoryNameChange = { _, _ -> },
        onTicketNameChange = { _, _, _ -> },
        onTicketEstimationChange = { _, _, _ -> },
    )
}
