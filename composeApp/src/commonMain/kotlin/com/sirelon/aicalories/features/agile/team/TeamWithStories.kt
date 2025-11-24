package com.sirelon.aicalories.features.agile.team

import com.sirelon.aicalories.features.agile.model.UserStory

data class TeamWithStories(
    val team: Team,
    val stories: List<UserStory>,
)
