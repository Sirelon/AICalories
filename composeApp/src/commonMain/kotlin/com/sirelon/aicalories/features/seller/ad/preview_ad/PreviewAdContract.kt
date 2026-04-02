package com.sirelon.aicalories.features.seller.ad.preview_ad

interface PreviewAdContract {
    data class PreviewAdState(
        val category: String,
        val minPrice: Float,
        val maxPrice: Float,
        val originalPrice: Double,
        val images: List<String>,
        val availableCategories: List<String> = DEFAULT_CATEGORIES,
        val isCategoryPickerVisible: Boolean = false,
    ) {
        companion object {
            val DEFAULT_CATEGORIES = listOf(
                "Electronics", "Fashion", "Home & Garden", "Vehicles",
                "Real Estate", "Sports & Hobbies", "Kids & Baby",
                "Books & Music", "Beauty & Health", "Pets", "Services", "Other"
            )
        }
    }

    sealed interface PreviewAdEvent {
        data class CategoryChanged(val category: String) : PreviewAdEvent
        data object ShowCategoryPicker : PreviewAdEvent
        data object DismissCategoryPicker : PreviewAdEvent
        data object Publish : PreviewAdEvent
    }

    sealed interface PreviewAdEffect {
        data class ShowMessage(val message: String) : PreviewAdEffect
    }
}
