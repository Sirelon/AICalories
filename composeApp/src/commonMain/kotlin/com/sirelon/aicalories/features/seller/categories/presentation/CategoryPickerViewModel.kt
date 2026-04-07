package com.sirelon.aicalories.features.seller.categories.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class CategoryPickerViewModel(
    private val parentCategory: OlxCategory?,
    private val categoriesRepository: CategoriesRepository,
) : BaseViewModel<CategoryPickerState, CategoryPickerEvent, Nothing>() {

    private val parentId: Int? = parentCategory?.id
    override fun initialState() = CategoryPickerState()

    init {
        val flow = if (parentId == null) {
            categoriesRepository.getRootCategories()
        } else {
            categoriesRepository.getSubcategories(parentId)
        }

        flow
            .map { all ->
                all.map { category ->
                    CategoryWithChildCount(
                        category = category,
                        // TODO: it's incorrect
                        childCount = all.count { it.parentId == category.id },
                    )
                }
            }
            .onEach { categories ->
                setState {
                    it.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CategoryPickerEvent) = Unit
}
