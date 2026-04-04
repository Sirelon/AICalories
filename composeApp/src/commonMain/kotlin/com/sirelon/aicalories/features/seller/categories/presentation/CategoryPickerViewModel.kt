package com.sirelon.aicalories.features.seller.categories.presentation

import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class CategoryPickerViewModel(
    private val parentId: Int?,
    private val categoriesRepository: CategoriesRepository,
) : BaseViewModel<CategoryPickerState, CategoryPickerEvent, Nothing>() {

    override fun initialState() = CategoryPickerState()

    init {
        categoriesRepository.loadCategories()
            .map { all ->
                val toDisplay = if (parentId == null) {
                    all.filter { it.parentId == null }
                } else {
                    all.filter { it.parentId == parentId }
                }
                toDisplay.map { category ->
                    CategoryWithChildCount(
                        category = category,
                        childCount = all.count { it.parentId == category.id },
                    )
                }
            }
            .onEach { categories -> setState { it.copy(categories = categories, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CategoryPickerEvent) = Unit
}
