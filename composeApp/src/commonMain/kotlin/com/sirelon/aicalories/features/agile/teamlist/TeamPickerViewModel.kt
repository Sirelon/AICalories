package com.sirelon.aicalories.features.agile.teamlist

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class TeamPickerViewModel(
    private val repository: AgileRepository,
) : BaseViewModel<
    TeamPickerContract.TeamPickerState,
    TeamPickerContract.TeamPickerEvent,
    TeamPickerContract.TeamPickerEffect,
    >() {

    override fun initialState(): TeamPickerContract.TeamPickerState =
        TeamPickerContract.TeamPickerState()

    init {
        println("[TeamPickerViewModel] init collecting teams")

        viewModelScope.launch {
            repository.observeTeamsWithStories().collectLatest { teams ->
                println("[TeamPickerViewModel] teams emitted size=${teams.size}")
                val teamItems = teams.map { teamWithStories ->
                    TeamPickerContract.TeamListItem(
                        team = teamWithStories.team,
                        storiesCount = teamWithStories.stories.size,
                        ticketsCount = teamWithStories.stories.sumOf { story -> story.tickets.size },
                    )
                }
                setState { currentState ->
                    currentState.copy(teams = teamItems)
                }
                println("[TeamPickerViewModel] state updated size=${teamItems.size}")
            }
        }
    }

    override fun onEvent(event: TeamPickerContract.TeamPickerEvent) {
        when (event) {
            TeamPickerContract.TeamPickerEvent.AddTeam -> repository.createTeam()

            is TeamPickerContract.TeamPickerEvent.RemoveTeam -> {
                if (state.value.teams.size > 1) {
                    repository.removeTeam(event.teamId)
                    println("[TeamPickerViewModel] remove team ${event.teamId}")
                }
            }
        }
    }
}
