package com.sirelon.aicalories.features.agile.capacity

import com.sirelon.aicalories.features.agile.EstimationResult
import com.sirelon.aicalories.features.agile.team.Team

interface CapacityResultContract {

    data class CapacityResultState(
        val teamId: Int,
        val team: Team? = null,
        val result: EstimationResult? = null,
    )

    sealed interface CapacityResultEvent {
        data object Refresh : CapacityResultEvent
    }
}
