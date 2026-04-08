package com.sirelon.aicalories.features.seller.ad.preview_ad

import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.OlxLocation
import kotlin.jvm.JvmInline

interface PreviewAdContract {

    data class PreviewAdState(
        val categoryLabel: String,
        val selectedCategory: OlxCategory? = null,
        val isPublishing: Boolean = false,
        val price: Float,
        val minPrice: Float,
        val maxPrice: Float,
        val images: List<String>,
        val attributes: List<OlxAttribute> = emptyList(),
        val location: OlxLocation? = null,
        val locationLoading: Boolean = false,
    )

    sealed interface PreviewAdEvent {
        data class CategorySelected(val category: OlxCategory) : PreviewAdEvent

        data object OnChangeCategoryClick : PreviewAdEvent
        data object Publish : PreviewAdEvent

        @JvmInline
        value class OnPriceChanged(val price: Float) : PreviewAdEvent

        data object FetchLocation : PreviewAdEvent
    }

    sealed interface PreviewAdEffect {
        data class ShowMessage(val message: String) : PreviewAdEffect
        data object GoToGategoryPicker : PreviewAdEffect
        data class PublishSuccess(val advertUrl: String?) : PreviewAdEffect
    }
}
