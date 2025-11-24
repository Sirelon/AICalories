package com.sirelon.aicalories.features.agile.presentation

import com.sirelon.aicalories.features.agile.Estimation
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.agile.model.Ticket
import com.sirelon.aicalories.features.agile.model.UserStory
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class AgileViewModel(
    private val teamId: Int,
    private val repository: AgileRepository,
) : BaseViewModel<AgileContract.AgileState, AgileContract.AgileEvent, AgileContract.AgileEffect>() {

    override fun initialState(): AgileContract.AgileState {
        val team = repository.getOrCreateTeam(teamId)
        val stories = repository.getTeamStories(teamId)
        return AgileContract.AgileState(
            teamId = teamId,
            team = team,
            stories = stories,
            nextStoryId = calculateNextStoryId(stories),
            nextTicketId = calculateNextTicketId(stories),
        )
    }

    init {
        viewModelScope.launch {
            repository.observeTeamWithStories(teamId)
                .collectLatest { teamWithStories ->
                    val stories = teamWithStories.stories.ifEmpty { createAndPersistDefaultStory() }
                    setState { currentState ->
                        currentState.copy(
                            teamId = teamId,
                            team = teamWithStories.team,
                            stories = stories,
                            nextStoryId = calculateNextStoryId(stories),
                            nextTicketId = calculateNextTicketId(stories),
                        )
                    }
                }
        }
    }

    override fun onEvent(event: AgileContract.AgileEvent) {
        when (event) {
            AgileContract.AgileEvent.AddUserStory -> addUserStory()
            is AgileContract.AgileEvent.AddTicket -> addTicket(event.storyId)
            is AgileContract.AgileEvent.StoryNameChanged -> updateStoryName(
                event.storyId,
                event.name
            )

            is AgileContract.AgileEvent.TicketNameChanged -> updateTicketName(
                event.storyId,
                event.ticketId,
                event.name
            )

            is AgileContract.AgileEvent.TicketEstimationChanged ->
                updateTicketEstimation(event.storyId, event.ticketId, event.estimation)

            is AgileContract.AgileEvent.TicketRemoved -> removeTicket(event.storyId, event.ticketId)
        }
    }

    private fun addUserStory() {
        setState { currentState ->
            val storyId = currentState.nextStoryId
            val newStory = UserStory(
                id = storyId,
                name = userStoryName(storyId),
                tickets = emptyList(),
            )
            val updatedStories = currentState.stories + newStory
            currentState
                .persistAndUpdateStories(updatedStories)
                .copy(nextStoryId = storyId + 1)
        }
    }

    private fun removeTicket(storyId: Int, ticketId: Int) {
        setState { currentState ->
            val updatedStories = currentState.stories.map {
                if (it.id == storyId) {
                    it.copy(tickets = it.tickets.filter { it.id != ticketId })
                } else {
                    it
                }
            }
            currentState.persistAndUpdateStories(updatedStories)
        }
    }

    private fun addTicket(storyId: Int) {
        setState { currentState ->
            val ticketId = currentState.nextTicketId
            val updatedStories = currentState.stories.map { story ->
                if (story.id == storyId) {
                    story.copy(
                        tickets = story.tickets + Ticket(
                            id = ticketId,
                            name = ticketName(ticketId),
                            estimation = defaultTicketEstimation(),
                        )
                    )
                } else {
                    story
                }
            }

            currentState
                .persistAndUpdateStories(updatedStories)
                .copy(nextTicketId = ticketId + 1)
        }
    }

    private fun updateStoryName(storyId: Int, name: String) {
        setState { currentState ->
            val updatedStories = currentState.stories.map { story ->
                if (story.id == storyId) {
                    story.copy(name = name)
                } else {
                    story
                }
            }
            currentState.persistAndUpdateStories(updatedStories)
        }
    }

    private fun updateTicketName(storyId: Int, ticketId: Int, name: String) {
        setState { currentState ->
            val updatedStories = currentState.stories.map { story ->
                if (story.id == storyId) {
                    val updatedTickets = story.tickets.map { ticket ->
                        if (ticket.id == ticketId) {
                            ticket.copy(name = name)
                        } else {
                            ticket
                        }
                    }
                    story.copy(tickets = updatedTickets)
                } else {
                    story
                }
            }
            currentState.persistAndUpdateStories(updatedStories)
        }
    }

    private fun updateTicketEstimation(storyId: Int, ticketId: Int, estimation: Estimation) {
        setState { currentState ->
            val updatedStories = currentState.stories.map { story ->
                if (story.id == storyId) {
                    val updatedTickets = story.tickets.map { ticket ->
                        if (ticket.id == ticketId) {
                            ticket.copy(estimation = estimation)
                        } else {
                            ticket
                        }
                    }
                    story.copy(tickets = updatedTickets)
                } else {
                    story
                }
            }
            currentState.persistAndUpdateStories(updatedStories)
        }
    }

    private fun AgileContract.AgileState.persistAndUpdateStories(
        updatedStories: List<UserStory>,
    ): AgileContract.AgileState {
        persistStories(updatedStories)
        return copy(
            stories = updatedStories,
        )
    }

    private fun persistStories(stories: List<UserStory>) {
        repository.saveStories(teamId, stories)
    }

    private fun createAndPersistDefaultStory(): List<UserStory> {
        val initialStoryId = 1
        val initialStory = UserStory(
            id = initialStoryId,
            name = userStoryName(initialStoryId),
            tickets = emptyList(),
        )
        val defaultStories = listOf(initialStory)
        persistStories(defaultStories)
        return defaultStories
    }

    private fun calculateNextStoryId(stories: List<UserStory>) =
        (stories.maxOfOrNull { it.id } ?: 0) + 1

    private fun calculateNextTicketId(stories: List<UserStory>) =
        (stories.flatMap { it.tickets }.maxOfOrNull { it.id } ?: 0) + 1

    private fun userStoryName(id: Int) = "User Story #$id"

    private fun ticketName(id: Int) = "Ticket #$id"

    private fun defaultTicketEstimation() = Estimation.M
}
