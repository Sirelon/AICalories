package com.sirelon.aicalories.navigation

import androidx.navigation3.runtime.NavKey
import com.sirelon.aicalories.features.seller.ad.publish_success.PublishSuccessData
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
        val data: PublishSuccessData,
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
