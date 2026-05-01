package com.sirelon.aicalories.features.seller.categories

import com.sirelon.aicalories.di.applicationScopeQualifier
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.categories.domain.AttributeValidator
import com.sirelon.aicalories.features.seller.categories.domain.CategoriesMapper
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val categoriesModule = module {
    factoryOf(::AttributeValidator)
    factoryOf(::CategoriesMapper)
    single { CategoriesRepository(get(), get(), get(applicationScopeQualifier)) }
    viewModel { params ->
        CategoryPickerViewModel(
            parentCategory = params.getOrNull<OlxCategory>(),
            categoriesRepository = get(),
        )
    }
}
