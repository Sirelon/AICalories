package com.sirelon.aicalories.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * High level destinations rendered by Navigation3.
 */
@Serializable
sealed interface AppDestination : NavKey {
    @Serializable
    data object Analyze : AppDestination

    @Serializable
    data object History : AppDestination
}
