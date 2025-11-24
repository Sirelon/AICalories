package com.sirelon.aicalories.features.agile.teamlist

import com.sirelon.aicalories.features.agile.team.Team

interface TeamPickerContract {

    data class TeamPickerState(
        val teams: List<TeamListItem> = emptyList(),
    )

    data class TeamListItem(
        val team: Team,
        val storiesCount: Int,
        val ticketsCount: Int,
    )

    sealed interface TeamPickerEvent {
        data object AddTeam : TeamPickerEvent
        data class RemoveTeam(val teamId: Int) : TeamPickerEvent
    }

    sealed interface TeamPickerEffect
}
