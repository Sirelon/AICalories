package com.sirelon.aicalories.features.agile.capacity

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.agile.EstimationCalculator
import com.sirelon.aicalories.features.agile.capacity.CapacityResultContract.CapacityResultEvent
import com.sirelon.aicalories.features.agile.capacity.CapacityResultEffect
import com.sirelon.aicalories.features.agile.capacity.CapacityResultContract.CapacityResultState
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class CapacityResultViewModel(
    private val teamId: Int,
    private val repository: AgileRepository,
    private val calculator: EstimationCalculator,
) : BaseViewModel<CapacityResultState, CapacityResultEvent, CapacityResultEffect>() {

    override fun initialState(): CapacityResultState = CapacityResultState(teamId = teamId)

    init {
        viewModelScope.launch {
            repository.observeTeamWithStories(teamId).collectLatest { teamWithStories ->
                val tickets = teamWithStories.stories.flatMap { it.tickets }
                val team = teamWithStories.team
                val result = calculator.evaluate(
                    tickets = tickets,
                    capacity = team.capacity,
                    riskFactor = team.riskFactor,
                )
                setState { currentState ->
                    currentState.copy(
                        team = team,
                        result = result,
                    )
                }
            }
        }
    }

    override fun onEvent(event: CapacityResultEvent) {
        when (event) {
            CapacityResultEvent.Refresh -> recalculate()
        }
    }

    private fun recalculate() {
        val snapshot = repository.getTeamWithStories(teamId)
        val tickets = snapshot.stories.flatMap { it.tickets }
        val team = snapshot.team
        val result = calculator.evaluate(
            tickets = tickets,
            capacity = team.capacity,
            riskFactor = team.riskFactor,
        )
        setState { currentState ->
            currentState.copy(team = team, result = result)
        }
    }
}
