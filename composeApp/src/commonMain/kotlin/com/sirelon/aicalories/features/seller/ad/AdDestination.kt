package com.sirelon.aicalories.features.seller.ad

import com.sirelon.aicalories.features.seller.ad.publish_success.PublishSuccessData
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import kotlinx.serialization.Serializable

sealed interface AdDestination {

    @Serializable
    data object GenerateAd : AdDestination

    @Serializable
    data class PreviewAd(val advertisement: AdvertisementWithAttributes) : AdDestination

    @Serializable
    data object SelectRootCategory : AdDestination

    @Serializable
    data class SelectSubcategory(val category: OlxCategory) : AdDestination

    @Serializable
    data object Profile : AdDestination

    @Serializable
    data class ProfileAuth(val url: String) : AdDestination


    @Serializable
    data class SellerPublishSuccess(
        val data: PublishSuccessData,
    ) : AdDestination

    @Serializable
    data class ImagesPreview(val images: List<String>, val initialPage: Int) : AdDestination
}