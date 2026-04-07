package com.sirelon.aicalories.features.seller.ad.preview_ad

import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttributeValue
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.domain.ValidationError
import com.sirelon.aicalories.features.seller.location.OlxLocation
import kotlin.jvm.JvmInline

data class OlxAttributeState(
    val attribute: OlxAttribute,
    val selectedValues: List<OlxAttributeValue> = emptyList(),
    val error: ValidationError? = null,
)

interface PreviewAdContract {

    data class PreviewAdState(
        val categoryLabel: String,
        val price: Float,
        val minPrice: Float,
        val maxPrice: Float,
        val images: List<String>,
        val location: OlxLocation? = null,
        val locationLoading: Boolean = false,
        val attributeItems: List<OlxAttributeState> = emptyList(),
    )

    sealed interface PreviewAdEvent {
        data class CategorySelected(val category: OlxCategory) : PreviewAdEvent

        data object OnChangeCategoryClick : PreviewAdEvent
        data object Publish : PreviewAdEvent

        @JvmInline
        value class OnPriceChanged(val price: Float) : PreviewAdEvent

        data object FetchLocation : PreviewAdEvent

        data class AttributeValueChanged(
            val attributeCode: String,
            val values: List<OlxAttributeValue>,
        ) : PreviewAdEvent
    }

    sealed interface PreviewAdEffect {
        data class ShowMessage(val message: String) : PreviewAdEffect
        data object GoToGategoryPicker : PreviewAdEffect
    }
}
