package com.sirelon.aicalories.features.agile.presentation

import com.sirelon.aicalories.features.agile.model.UserStory

interface AgileContract {

    data class AgileState(
        val stories: List<UserStory> = emptyList(),
        val nextStoryId: Int = 1,
        val nextTicketId: Int = 1,
    )

    sealed interface AgileEvent {
        data object AddUserStory : AgileEvent
        data class AddTicket(val storyId: Int) : AgileEvent
        data class StoryNameChanged(val storyId: Int, val name: String) : AgileEvent
        data class TicketNameChanged(val storyId: Int, val ticketId: Int, val name: String) : AgileEvent
    }

    sealed interface AgileEffect
}
