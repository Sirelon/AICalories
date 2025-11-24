package com.sirelon.aicalories.features.agile.team

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class TeamViewModel(
    private val teamId: Int,
    private val repository: AgileRepository,
) : BaseViewModel<TeamContract.TeamState, TeamContract.TeamEvent, TeamContract.TeamEffect>() {

    override fun initialState(): TeamContract.TeamState {
        val team = repository.getOrCreateTeam(teamId)
        return TeamContract.TeamState(team = team)
    }

    init {
        repository
            .observeTeam(teamId)
            .onEach { team ->
                setState { it.copy(team = team) }
            }
            .launchIn(viewModelScope)
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

            is TeamContract.TeamEvent.RiskFactorChanged -> updateTeam(
                event.teamId,
                transform = { team ->
                    team.copy(
                        riskFactor = sanitizeRiskFactor(event.riskFactor),
                    )
                },
            )
        }
    }

    private fun updateTeam(teamId: Int, transform: (Team) -> Team) {
        val currentState = state.value
        val updatedTeam = transform(currentState.team)
        repository.updateTeam(updatedTeam)
    }

    private fun sanitizeNumber(input: String): Int =
        input.filter { it.isDigit() }.toIntOrNull() ?: 0

    private fun sanitizeRiskFactor(input: String): Double {
        val normalized = input.replace(',', '.')
        val value = normalized.toDoubleOrNull() ?: 0.0
        return value.coerceIn(0.0, 1.0)
    }
}
