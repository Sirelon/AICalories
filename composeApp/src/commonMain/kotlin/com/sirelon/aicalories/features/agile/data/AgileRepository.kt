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
    private var nextTeamId = 1

    init {
        ensureTeam(DEFAULT_TEAM_ID)
    }

    fun observeTeams(): Flow<List<Team>> =
        teams.map { teamMap ->
            teamMap.values.sortedBy { it.id }
        }.distinctUntilChanged()

    fun observeTeamsWithStories(): Flow<List<TeamWithStories>> =
        combine(teams, teamStories) { teamMap, storiesMap ->
            teamMap.values.sortedBy { it.id }.map { team ->
                TeamWithStories(
                    team = team,
                    stories = storiesMap[team.id].orEmpty(),
                )
            }
        }.distinctUntilChanged()

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

    fun createTeam(): Team {
        val id = generateTeamId()
        val team = defaultTeam(id)
        teams.update { existing ->
            existing + (id to team)
        }
        return team
    }

    fun removeTeam(teamId: Int) {
        teams.update { existing ->
            existing - teamId
        }
        teamStories.update { existing ->
            existing - teamId
        }
        if (teams.value.isEmpty()) {
            ensureTeam(DEFAULT_TEAM_ID)
        }
    }

    private fun ensureTeam(teamId: Int): Team =
        teams.value[teamId] ?: defaultTeam(teamId).also { team ->
            teams.update { existing ->
                existing + (teamId to team)
            }
            synchronized(this) {
                if (teamId >= nextTeamId) {
                    nextTeamId = teamId + 1
                }
            }
        }

    private fun generateTeamId(): Int = synchronized(this) {
        val id = nextTeamId
        nextTeamId += 1
        id
    }

    private fun defaultTeam(id: Int) = Team(
        id = id,
        name = "Team #$id",
        peopleCount = 5,
        capacity = 40,
        riskFactor = Team.DEFAULT_RISK_FACTOR,
    )

    companion object {
        private const val DEFAULT_TEAM_ID = 1
    }
}
