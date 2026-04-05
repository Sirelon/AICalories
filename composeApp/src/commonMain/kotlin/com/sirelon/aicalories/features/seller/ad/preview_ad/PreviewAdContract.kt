package com.sirelon.aicalories.features.seller.ad.preview_ad

import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.OlxLocation

interface PreviewAdContract {

    data class PreviewAdState(
        val categoryLabel: String,
        val minPrice: Float,
        val maxPrice: Float,
        val originalPrice: Double,
        val images: List<String>,
        val location: OlxLocation? = null,
        val locationLoading: Boolean = false,
    )

    sealed interface PreviewAdEvent {
        data class CategorySelected(val category: OlxCategory) : PreviewAdEvent
        data object Publish : PreviewAdEvent
        data object FetchLocation : PreviewAdEvent
    }

    sealed interface PreviewAdEffect {
        data class ShowMessage(val message: String) : PreviewAdEffect
    }
}
