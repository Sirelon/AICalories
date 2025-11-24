package com.sirelon.aicalories.features.agile.presentation

import com.sirelon.aicalories.features.agile.Estimation
import com.sirelon.aicalories.features.agile.model.UserStory

interface AgileContract {

    data class AgileState(
        val teamId: Int = 1,
        val stories: List<UserStory> = emptyList(),
        val nextStoryId: Int = 1,
        val nextTicketId: Int = 1,
    )

    sealed interface AgileEvent {
        data object AddUserStory : AgileEvent
        data object CalculateCapacity : AgileEvent
        data class AddTicket(val storyId: Int) : AgileEvent
        data class StoryNameChanged(val storyId: Int, val name: String) : AgileEvent
        data class TicketNameChanged(val storyId: Int, val ticketId: Int, val name: String) : AgileEvent
        data class TicketRemoved(val storyId: Int, val ticketId: Int) : AgileEvent
        data class TicketEstimationChanged(
            val storyId: Int,
            val ticketId: Int,
            val estimation: Estimation,
        ) : AgileEvent
    }

    sealed interface AgileEffect
}
