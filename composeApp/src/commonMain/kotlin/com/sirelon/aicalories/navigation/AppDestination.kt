package com.sirelon.aicalories.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * High level destinations rendered by Navigation3.
 */
@Serializable
sealed interface AppDestination : NavKey {

    @Serializable
    data object Splash : AppDestination

    data object SellerOnboarding : AppDestination

    @Serializable
    data object SellerLanding : AppDestination

    @Serializable
    data object Seller : AppDestination

    @Serializable
    data class SellerPublishSuccess(
        val url: String,
        val title: String,
        val priceFormatted: String,
        val primaryImageUrl: String?,
    ) : AppDestination

    @Serializable
    data object Analyze : AppDestination

    @Serializable
    data object History : AppDestination


    @Serializable
    data object Agile : AppDestination

    @Serializable
    data object DataGenerator : AppDestination
}
