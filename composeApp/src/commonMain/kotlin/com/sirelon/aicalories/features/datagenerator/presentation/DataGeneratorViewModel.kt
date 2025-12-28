package com.sirelon.aicalories.features.datagenerator.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.agile.data.AgileRepository
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.datagenerator.data.RandomDataGenerator
import com.sirelon.aicalories.features.datagenerator.model.GenerationConfig
import com.sirelon.aicalories.features.datagenerator.model.IntRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class DataGeneratorViewModel(
    private val repository: AgileRepository,
    private val dataGenerator: RandomDataGenerator
) : BaseViewModel<DataGeneratorContract.DataGeneratorState,
        DataGeneratorContract.DataGeneratorEvent,
        DataGeneratorContract.DataGeneratorEffect
        >() {

    override fun initialState(): DataGeneratorContract.DataGeneratorState {
        val normalized = normalizeConfig(GenerationConfig())
        return buildState(normalized)
    }

    init {
        repository.observeTeams()
            .onEach { teams ->
                setState { it.copy(existingTeamsCount = teams.size) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: DataGeneratorContract.DataGeneratorEvent) {
        when (event) {
            is DataGeneratorContract.DataGeneratorEvent.TeamsCountChanged ->
                updateConfig { copy(teamsCount = event.value.toIntOrNull() ?: teamsCount) }

            is DataGeneratorContract.DataGeneratorEvent.StoriesPerTeamChanged ->
                updateConfig {
                    copy(
                        storiesPerTeamCount = event.value.toIntOrNull() ?: storiesPerTeamCount
                    )
                }

            is DataGeneratorContract.DataGeneratorEvent.TicketsPerStoryRangeChanged ->
                updateConfig { copy(ticketsPerStory = event.range) }

            is DataGeneratorContract.DataGeneratorEvent.TeamCapacityRangeChanged ->
                updateConfig { copy(teamCapacity = event.range) }

            is DataGeneratorContract.DataGeneratorEvent.TeamPeopleCountRangeChanged ->
                updateConfig { copy(teamPeopleCount = event.range) }

            is DataGeneratorContract.DataGeneratorEvent.TeamRiskFactorRangeChanged ->
                updateConfig { copy(teamRiskFactor = event.range) }

            is DataGeneratorContract.DataGeneratorEvent.ClearExistingDataChanged ->
                updateConfig { copy(clearExistingData = event.checked) }

            DataGeneratorContract.DataGeneratorEvent.GenerateRandomData -> generateData()
            DataGeneratorContract.DataGeneratorEvent.ResetToEmpty -> resetData()
        }
    }

    private fun updateConfig(transform: GenerationConfig.() -> GenerationConfig) {
        setState { current ->
            val updated = current.config.transform()
            val normalized = normalizeConfig(updated)
            buildState(normalized, current)
        }
    }

    private fun buildState(
        config: GenerationConfig,
        current: DataGeneratorContract.DataGeneratorState? = null
    ): DataGeneratorContract.DataGeneratorState {
        val peopleBounds = PEOPLE_PER_TEAM_BOUNDS
        val capacityMax = (config.teamPeopleCount.max * 10).coerceAtLeast(1)
        val capacityBounds = IntRange(min = 1, max = capacityMax)
        return if (current == null) {
            DataGeneratorContract.DataGeneratorState(
                config = config,
                peoplePerTeamBounds = peopleBounds,
                teamCapacityBounds = capacityBounds
            )
        } else {
            current.copy(
                config = config,
                peoplePerTeamBounds = peopleBounds,
                teamCapacityBounds = capacityBounds
            )
        }
    }

    private fun normalizeConfig(config: GenerationConfig): GenerationConfig {
        val peopleBounds = PEOPLE_PER_TEAM_BOUNDS
        val peopleMin = config.teamPeopleCount.min.coerceIn(peopleBounds.min, peopleBounds.max)
        val peopleMax = config.teamPeopleCount.max.coerceIn(peopleBounds.min, peopleBounds.max)
        val normalizedPeopleMin = minOf(peopleMin, peopleMax)
        val normalizedPeopleMax = maxOf(peopleMin, peopleMax)
        val normalizedPeople = IntRange(normalizedPeopleMin, normalizedPeopleMax)

        val capacityMaxAllowed = (normalizedPeopleMax * 10).coerceAtLeast(1)
        val capacityMin = config.teamCapacity.min.coerceIn(1, capacityMaxAllowed)
        val capacityMax = config.teamCapacity.max.coerceIn(1, capacityMaxAllowed)
        val normalizedCapacityMin = minOf(capacityMin, capacityMax)
        val normalizedCapacityMax = maxOf(capacityMin, capacityMax)
        val normalizedCapacity = IntRange(normalizedCapacityMin, normalizedCapacityMax)

        return config.copy(
            teamPeopleCount = normalizedPeople,
            teamCapacity = normalizedCapacity
        )
    }

    private fun generateData() {
        val config = state.value.config

        val validationError = validateConfig(config)
        if (validationError != null) {
            postEffect(DataGeneratorContract.DataGeneratorEffect.ShowError(validationError))
            return
        }

        setState { it.copy(isGenerating = true) }

        viewModelScope.launch {
            try {
                // Clear existing data if requested
                if (config.clearExistingData) {
                    clearAllData()
                }

                val startingIds = calculateStartingIds()
                generateAndSaveTeams(config, startingIds)

                postEffect(DataGeneratorContract.DataGeneratorEffect.DataGenerated)
            } catch (e: Exception) {
                postEffect(
                    DataGeneratorContract.DataGeneratorEffect.ShowError(
                        e.message ?: "Generation failed"
                    )
                )
            } finally {
                setState { it.copy(isGenerating = false) }
            }
        }
    }

    private fun resetData() {
        setState { it.copy(isGenerating = true) }
        viewModelScope.launch {
            try {
                clearAllData()
                postEffect(DataGeneratorContract.DataGeneratorEffect.DataCleared)
            } catch (e: Exception) {
                postEffect(
                    DataGeneratorContract.DataGeneratorEffect.ShowError(
                        e.message ?: "Reset failed"
                    )
                )
            } finally {
                setState { it.copy(isGenerating = false) }
            }
        }
    }

    private suspend fun clearAllData() {
        val teams = repository.observeTeams().first()
        teams.forEach { team ->
            repository.removeTeam(team.id)
        }
    }

    private fun validateConfig(config: GenerationConfig): String? {
        return when {
            config.teamsCount < 1 -> "Teams count must be at least 1"
            config.storiesPerTeamCount < 1 -> "Stories per team must be at least 1"
            config.ticketsPerStory.min < 1 -> "Min tickets must be at least 1"
            config.ticketsPerStory.min > config.ticketsPerStory.max ->
                "Min tickets cannot exceed max tickets"

            config.teamCapacity.min > config.teamCapacity.max ->
                "Min capacity cannot exceed max capacity"

            config.teamPeopleCount.min > config.teamPeopleCount.max ->
                "Min people count cannot exceed max"

            config.teamRiskFactor.min > config.teamRiskFactor.max ->
                "Min risk factor cannot exceed max"

            else -> null
        }
    }

    private suspend fun generateAndSaveTeams(
        config: GenerationConfig,
        startingIds: StartingIds,
    ) {
        var globalNextTicketId = startingIds.ticketId
        var globalNextStoryId = startingIds.storyId
        val newTeams = dataGenerator.generateTeams(config, startingIds.teamId)

        newTeams.forEach { team ->
            repository.createTeam()
            repository.updateTeam(team)

            val (stories, nextTicketId) = dataGenerator.generateStoriesForTeam(
                config,
                globalNextStoryId,
                globalNextTicketId
            )

            repository.saveStories(team.id, stories)

            globalNextStoryId += stories.size
            globalNextTicketId = nextTicketId
        }
    }

    private suspend fun calculateStartingIds(): StartingIds {
        val existingTeams = repository.observeTeams().first()
        val startingTeamId = (existingTeams.maxOfOrNull { it.id } ?: 0) + 1
        var nextTicketId = 1
        var nextStoryId = 1

        existingTeams.forEach { team ->
            val stories = repository.getTeamStories(team.id)
            stories.forEach { story ->
                if (story.id >= nextStoryId) {
                    nextStoryId = story.id + 1
                }
                story.tickets.forEach { ticket ->
                    if (ticket.id >= nextTicketId) {
                        nextTicketId = ticket.id + 1
                    }
                }
            }
        }

        return StartingIds(
            teamId = startingTeamId,
            storyId = nextStoryId,
            ticketId = nextTicketId
        )
    }

    private data class StartingIds(
        val teamId: Int,
        val storyId: Int,
        val ticketId: Int,
    )

    private companion object {
        private val PEOPLE_PER_TEAM_BOUNDS = IntRange(min = 1, max = 12)
    }
}
