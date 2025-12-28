package com.sirelon.aicalories.features.datagenerator.presentation

import com.sirelon.aicalories.features.datagenerator.model.DoubleRange
import com.sirelon.aicalories.features.datagenerator.model.GenerationConfig
import com.sirelon.aicalories.features.datagenerator.model.IntRange

interface DataGeneratorContract {

    data class DataGeneratorState(
        val config: GenerationConfig = GenerationConfig(),
        val isGenerating: Boolean = false,
        val existingTeamsCount: Int = 0,
        val peoplePerTeamBounds: IntRange = IntRange(min = 1, max = 12),
        val teamCapacityBounds: IntRange = IntRange(min = 1, max = 120)
    )

    sealed interface DataGeneratorEvent {
        data class TeamsCountChanged(val value: String) : DataGeneratorEvent
        data class StoriesPerTeamChanged(val value: String) : DataGeneratorEvent
        data class TicketsPerStoryRangeChanged(val range: IntRange) : DataGeneratorEvent
        data class TeamCapacityRangeChanged(val range: IntRange) : DataGeneratorEvent
        data class TeamPeopleCountRangeChanged(val range: IntRange) : DataGeneratorEvent
        data class TeamRiskFactorRangeChanged(val range: DoubleRange) : DataGeneratorEvent
        data class ClearExistingDataChanged(val checked: Boolean) : DataGeneratorEvent
        data object GenerateRandomData : DataGeneratorEvent
        data object ResetToEmpty : DataGeneratorEvent
    }

    sealed interface DataGeneratorEffect {
        data object DataGenerated : DataGeneratorEffect
        data object DataCleared : DataGeneratorEffect
        data class ShowError(val message: String) : DataGeneratorEffect
    }
}
