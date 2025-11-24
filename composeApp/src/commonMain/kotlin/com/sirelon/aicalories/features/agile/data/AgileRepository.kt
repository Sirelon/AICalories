package com.sirelon.aicalories.features.agile.data

import com.sirelon.aicalories.features.agile.model.UserStory
import com.sirelon.aicalories.features.agile.team.Team
import com.sirelon.aicalories.features.agile.team.TeamWithStories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Temporary in-memory storage for agile entities.
 * Later this can be replaced with persistence.
 */
class AgileRepository {

    private val teams = MutableStateFlow<Map<Int, Team>>(emptyMap())
    private val teamStories = MutableStateFlow<Map<Int, List<UserStory>>>(emptyMap())

    fun observeTeam(teamId: Int): Flow<Team> {
        ensureTeam(teamId)
        return teams.map { it[teamId] ?: defaultTeam(teamId) }.distinctUntilChanged()
    }

    fun getOrCreateTeam(teamId: Int): Team = ensureTeam(teamId)

    fun getTeamStories(teamId: Int): List<UserStory> =
        teamStories.value[teamId].orEmpty()

    fun getTeamWithStories(teamId: Int): TeamWithStories =
        TeamWithStories(
            team = getOrCreateTeam(teamId),
            stories = getTeamStories(teamId),
        )

    fun observeTeamWithStories(teamId: Int): Flow<TeamWithStories> {
        ensureTeam(teamId)
        return combine(teams, teamStories) { teamMap, storiesMap ->
            TeamWithStories(
                team = teamMap[teamId] ?: defaultTeam(teamId),
                stories = storiesMap[teamId].orEmpty(),
            )
        }.distinctUntilChanged()
    }

    fun updateTeam(team: Team) {
        ensureTeam(team.id)
        teams.update { existing ->
            existing + (team.id to team)
        }
    }

    fun saveStories(teamId: Int, stories: List<UserStory>) {
        ensureTeam(teamId) // ensure team is present
        teamStories.update { existing ->
            existing + (teamId to stories)
        }
    }

    private fun ensureTeam(teamId: Int): Team =
        teams.value[teamId] ?: defaultTeam(teamId).also { team ->
            teams.update { existing ->
                existing + (teamId to team)
            }
        }

    private fun defaultTeam(id: Int) = Team(
        id = id,
        name = "Team #$id",
        peopleCount = 5,
        capacity = 40,
    )
}
