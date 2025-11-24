package com.sirelon.aicalories.features.agile.team

/**
 * Represents a delivery team and its capacity.
 */
data class Team(
    val id: Int,
    val name: String,
    val peopleCount: Int,
    val capacity: Int,
    val riskFactor: Double = DEFAULT_RISK_FACTOR,
) {
    companion object {
        const val DEFAULT_TEAM_ID = 1
        const val DEFAULT_RISK_FACTOR = 0.2
        const val DEFAULT_TEAM_CAPACITY = 10
        const val DEFAULT_TEAM_PEOPLE_COUNT = 5

        fun default(id: Int = DEFAULT_TEAM_ID): Team {
            return Team(
                id = id,
                name = "Team #$id",
                peopleCount = DEFAULT_TEAM_PEOPLE_COUNT,
                capacity = DEFAULT_TEAM_CAPACITY,
                riskFactor = DEFAULT_RISK_FACTOR,
            )
        }
    }
}
