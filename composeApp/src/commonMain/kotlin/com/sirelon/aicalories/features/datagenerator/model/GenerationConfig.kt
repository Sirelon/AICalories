package com.sirelon.aicalories.features.datagenerator.model

data class IntRange(
    val min: Int,
    val max: Int
) {
    init {
        require(min <= max) { "Min value ($min) cannot be greater than max value ($max)" }
    }
}

data class DoubleRange(
    val min: Double,
    val max: Double
) {
    init {
        require(min <= max) { "Min value ($min) cannot be greater than max value ($max)" }
    }
}

data class GenerationConfig(
    val teamsCount: Int = 3,
    val storiesPerTeamCount: Int = 5,
    val ticketsPerStory: IntRange = IntRange(min = 3, max = 8),
    val teamCapacity: IntRange = IntRange(min = 8, max = 15),
    val teamPeopleCount: IntRange = IntRange(min = 3, max = 8),
    val teamRiskFactor: DoubleRange = DoubleRange(min = 0.1, max = 0.3),
    val clearExistingData: Boolean = false
)
