package com.sirelon.aicalories.features.agile.presentation

import com.sirelon.aicalories.features.common.presentation.BaseViewModel

internal class AgileViewModel :
    BaseViewModel<AgileContract.AgileState, AgileContract.AgileEvent, AgileContract.AgileEffect>() {

    override fun initialState(): AgileContract.AgileState {
        val initialStoryId = 1
        val initialStory = AgileContract.UserStory(
            id = initialStoryId,
            name = userStoryName(initialStoryId),
            tickets = emptyList(),
        )
        return AgileContract.AgileState(
            stories = listOf(initialStory),
            nextStoryId = initialStoryId + 1,
            nextTicketId = 1,
        )
    }

    override fun onEvent(event: AgileContract.AgileEvent) {
        when (event) {
            AgileContract.AgileEvent.AddUserStory -> addUserStory()
            is AgileContract.AgileEvent.AddTicket -> addTicket(event.storyId)
            is AgileContract.AgileEvent.StoryNameChanged -> updateStoryName(event.storyId, event.name)
            is AgileContract.AgileEvent.TicketNameChanged -> updateTicketName(event.storyId, event.ticketId, event.name)
        }
    }

    private fun addUserStory() {
        setState { currentState ->
            val storyId = currentState.nextStoryId
            val newStory = AgileContract.UserStory(
                id = storyId,
                name = userStoryName(storyId),
                tickets = emptyList(),
            )
            currentState.copy(
                stories = currentState.stories + newStory,
                nextStoryId = storyId + 1,
            )
        }
    }

    private fun addTicket(storyId: Int) {
        setState { currentState ->
            val ticketId = currentState.nextTicketId
            val updatedStories = currentState.stories.map { story ->
                if (story.id == storyId) {
                    story.copy(
                        tickets = story.tickets + AgileContract.Ticket(
                            id = ticketId,
                            name = ticketName(ticketId),
                        )
                    )
                } else {
                    story
                }
            }

            currentState.copy(
                stories = updatedStories,
                nextTicketId = ticketId + 1,
            )
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
            currentState.copy(stories = updatedStories)
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
            currentState.copy(stories = updatedStories)
        }
    }

    private fun userStoryName(id: Int) = "User Story #$id"

    private fun ticketName(id: Int) = "Ticket #$id"
}
