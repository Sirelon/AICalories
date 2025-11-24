package com.sirelon.aicalories.features.agile.teamlist

import com.sirelon.aicalories.features.agile.team.Team

interface TeamPickerContract {

    data class TeamPickerState(
        val teams: List<Team> = emptyList(),
    )

    sealed interface TeamPickerEvent {
        data object AddTeam : TeamPickerEvent
        data class RemoveTeam(val teamId: Int) : TeamPickerEvent
    }

    sealed interface TeamPickerEffect
}
