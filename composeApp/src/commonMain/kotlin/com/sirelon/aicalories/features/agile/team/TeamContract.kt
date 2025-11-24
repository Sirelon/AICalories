package com.sirelon.aicalories.features.agile.team

interface TeamContract {

    data class TeamState(
        val team: Team,
    )

    sealed interface TeamEvent {
        data class NameChanged(val teamId: Int, val name: String) : TeamEvent
        data class PeopleCountChanged(val teamId: Int, val peopleCount: String) : TeamEvent
        data class CapacityChanged(val teamId: Int, val capacity: String) : TeamEvent
        data class RiskFactorChanged(val teamId: Int, val riskFactor: String) : TeamEvent
    }

    sealed interface TeamEffect
}
