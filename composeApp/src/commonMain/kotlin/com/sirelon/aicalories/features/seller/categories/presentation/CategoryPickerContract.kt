package com.sirelon.aicalories.features.seller.categories.presentation

import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory

data class CategoryWithChildCount(
    val category: OlxCategory,
    val childCount: Int,
)

interface CategoryPickerContract {

    data class CategoryPickerState(
        val categories: List<CategoryWithChildCount> = emptyList(),
        val isLoading: Boolean = true,
    )

    sealed interface CategoryPickerEvent
}
