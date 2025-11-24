package com.sirelon.aicalories.features.agile.team

import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel

internal class TeamViewModel(
    private val teamId: Int,
    private val repository: AgileRepository,
) : BaseViewModel<TeamContract.TeamState, TeamContract.TeamEvent, TeamContract.TeamEffect>() {

    override fun initialState(): TeamContract.TeamState {
        val team = repository.getOrCreateTeam(teamId)
        return TeamContract.TeamState(team = team)
    }

    override fun onEvent(event: TeamContract.TeamEvent) {
        when (event) {
            is TeamContract.TeamEvent.CapacityChanged -> updateTeam(
                event.teamId,
                transform = { team ->
                    team.copy(
                        capacity = sanitizeNumber(event.capacity),
                    )
                },
            )

            is TeamContract.TeamEvent.NameChanged -> updateTeam(
                event.teamId,
                transform = { team ->
                    team.copy(name = event.name)
                },
            )

            is TeamContract.TeamEvent.PeopleCountChanged -> updateTeam(
                event.teamId,
                transform = { team ->
                    team.copy(
                        peopleCount = sanitizeNumber(event.peopleCount),
                    )
                },
            )
        }
    }

    private fun updateTeam(teamId: Int, transform: (Team) -> Team) {
        setState { currentState ->
            if (currentState.team.id != teamId) return@setState currentState
            val updatedTeam = transform(currentState.team)
            repository.updateTeam(updatedTeam)
            currentState.copy(team = updatedTeam)
        }
    }

    private fun sanitizeNumber(input: String): Int =
        input.filter { it.isDigit() }.toIntOrNull() ?: 0
}
