package com.sirelon.aicalories.features.agile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun AgileScreenContent(
    state: AgileContract.AgileState,
    onBack: () -> Unit,
    onEvent: (AgileContract.AgileEvent) -> Unit,
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
                onClick = { onEvent(AgileContract.AgileEvent.AddUserStory) },
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
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    title = {
                        Input(
                            modifier = Modifier.fillMaxWidth(),
                            value = story.name,
                            onValueChange = {
                                onEvent(
                                    AgileContract.AgileEvent.StoryNameChanged(
                                        storyId = story.id,
                                        name = it,
                                    )
                                )
                            },
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier.alpha(0.85f).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
                    ) {
                        TicketsList(story = story, onEvent = onEvent)
                        TextButton(
                            onClick = {
                                onEvent(AgileContract.AgileEvent.AddTicket(story.id))
                            },
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
private fun TicketsList(
    story: UserStory,
    onEvent: (AgileContract.AgileEvent) -> Unit
) {
    story.tickets.forEach { ticket ->
        key(ticket.id) {
            TicketInput(
                modifier = Modifier
                    .padding(start = AppDimens.Spacing.xl3),
                ticket = ticket,
                onTicketNameChange = { name ->
                    onEvent(
                        AgileContract.AgileEvent.TicketNameChanged(
                            storyId = story.id,
                            ticketId = ticket.id,
                            name = name,
                        )
                    )
                },
                onTicketEstimationChange = { estimationValue ->
                    onEvent(
                        AgileContract.AgileEvent.TicketEstimationChanged(
                            storyId = story.id,
                            ticketId = ticket.id,
                            estimation = estimationValue,
                        )
                    )
                },
                onTicketRemoved = {
                    onEvent(
                        AgileContract.AgileEvent.TicketRemoved(
                            storyId = story.id,
                            ticketId = ticket.id,
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun TicketInput(
    ticket: Ticket,
    onTicketNameChange: (String) -> Unit,
    onTicketEstimationChange: (Estimation) -> Unit,
    onTicketRemoved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (isSheetOpen, setSheetVisible) = remember { mutableStateOf(false) }

    Input(
        modifier = modifier.fillMaxWidth(),
        value = ticket.name,
        onValueChange = onTicketNameChange,
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = AppDimens.Spacing.s
                )
            ) {
                TicketEstimationTrailing(
                    estimation = ticket.estimation,
                    onClick = { setSheetVisible(true) },
                )
                IconButton(onClick = onTicketRemoved) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

    if (isSheetOpen) {
        EstimationPickerSheet(
            selected = ticket.estimation,
            onSelected = {
                onTicketEstimationChange(it)
                setSheetVisible(false)
            },
            onDismissRequest = { setSheetVisible(false) },
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
        onEvent = {},
    )
}
