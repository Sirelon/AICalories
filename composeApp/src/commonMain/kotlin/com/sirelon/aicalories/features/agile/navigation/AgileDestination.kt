package com.sirelon.aicalories.features.agile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Destinations that belong to the Agile navigation graph.
 */
@Serializable
sealed interface AgileDestination : NavKey {
    val title: String

    @Serializable
    data object TeamPicker : AgileDestination {
        override val title: String = "Teams"
    }

    @Serializable
    data class StoryBoard(val teamId: Int) : AgileDestination {
        override val title: String = "Agile"
    }

    @Serializable
    data class TeamSettings(val teamId: Int) : AgileDestination {
        override val title: String = "Team settings"
    }

    @Serializable
    data class CapacityResult(val teamId: Int) : AgileDestination {
        override val title: String = "Capacity result"
    }
}
