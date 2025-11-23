package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar
import com.sirelon.aicalories.designsystem.Input
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
                UserStoryCard(
                    story = story,
                    onStoryNameChange = { onStoryNameChange(story.id, it) },
                    onAddTicket = { onAddTicket(story.id) },
                    onTicketNameChange = { ticketId, value ->
                        onTicketNameChange(story.id, ticketId, value)
                    },
                )
            }
        }
    }
}

@Composable
private fun UserStoryCard(
    story: AgileContract.UserStory,
    onStoryNameChange: (String) -> Unit,
    onAddTicket: () -> Unit,
    onTicketNameChange: (Int, String) -> Unit,
) {
    var estimation by rememberSaveable(story.id) { mutableStateOf(Estimation.M) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            EstimationChooser(
                selected = estimation,
                onSelected = { estimation = it },
            )

            Input(
                modifier = Modifier.fillMaxWidth(),
                value = story.name,
                onValueChange = onStoryNameChange,
                singleLine = true,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
            ) {
                story.tickets.forEach { ticket ->
                    Input(
                        modifier = Modifier.fillMaxWidth(),
                        value = ticket.name,
                        onValueChange = { onTicketNameChange(ticket.id, it) },
                        singleLine = true,
                    )
                }
            }
            TextButton(
                onClick = onAddTicket,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = AppDimens.Spacing.m),
                        text = "Add ticket",
                    )
                },
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
                AgileContract.UserStory(
                    id = 1,
                    name = "User Story #1",
                    tickets = listOf(
                        AgileContract.Ticket(id = 1, name = "Ticket #1"),
                        AgileContract.Ticket(id = 2, name = "Ticket #2"),
                    ),
                ),
                AgileContract.UserStory(
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
    )
}
