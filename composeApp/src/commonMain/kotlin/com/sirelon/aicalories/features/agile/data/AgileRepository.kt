package com.sirelon.aicalories.features.agile.data

import com.sirelon.aicalories.features.agile.team.Team

/**
 * Temporary in-memory storage for agile entities.
 * Later this can be replaced with persistence.
 */
class AgileRepository {

    private val teams = mutableMapOf<Int, Team>()

    fun getOrCreateTeam(teamId: Int): Team {
        return teams.getOrPut(teamId) { defaultTeam(teamId) }
    }

    fun updateTeam(team: Team) {
        teams[team.id] = team
    }

    private fun defaultTeam(id: Int) = Team(
        id = id,
        name = "Team #$id",
        peopleCount = 5,
        capacity = 40,
    )
}
