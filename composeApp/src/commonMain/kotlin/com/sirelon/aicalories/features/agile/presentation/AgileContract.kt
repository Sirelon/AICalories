package com.sirelon.aicalories.features.agile.presentation

import com.sirelon.aicalories.features.agile.Estimation

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
        val estimation: Estimation = Estimation.M,
    )

    sealed interface AgileEvent {
        data object AddUserStory : AgileEvent
        data class AddTicket(val storyId: Int) : AgileEvent
        data class StoryNameChanged(val storyId: Int, val name: String) : AgileEvent
        data class TicketNameChanged(val storyId: Int, val ticketId: Int, val name: String) : AgileEvent
        data class TicketEstimationChanged(
            val storyId: Int,
            val ticketId: Int,
            val estimation: Estimation,
        ) : AgileEvent
    }

    sealed interface AgileEffect
}
