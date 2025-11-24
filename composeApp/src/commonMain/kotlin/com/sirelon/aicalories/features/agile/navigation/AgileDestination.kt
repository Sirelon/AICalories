package com.sirelon.aicalories.features.agile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Destinations that belong to the Agile navigation graph.
 */
@Serializable
sealed interface AgileDestination : NavKey {
    @Serializable
    data object StoryBoard : AgileDestination

    @Serializable
    data class TeamSettings(val teamId: Int) : AgileDestination
}
