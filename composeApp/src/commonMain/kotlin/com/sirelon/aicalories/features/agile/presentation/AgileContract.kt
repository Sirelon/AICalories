package com.sirelon.aicalories.features.agile.presentation

interface AgileContract {

    data class AgileState(
        val stories: List<UserStory> = emptyList(),
        val nextStoryId: Int = 1,
        val nextTicketId: Int = 1,
    )

    data class UserStory(
        val id: Int,
        val name: String,
        val tickets: List<Ticket>,
    )

    data class Ticket(
        val id: Int,
        val name: String,
    )

    sealed interface AgileEvent {
        data object AddUserStory : AgileEvent
        data class AddTicket(val storyId: Int) : AgileEvent
        data class StoryNameChanged(val storyId: Int, val name: String) : AgileEvent
        data class TicketNameChanged(val storyId: Int, val ticketId: Int, val name: String) : AgileEvent
    }

    sealed interface AgileEffect
}
