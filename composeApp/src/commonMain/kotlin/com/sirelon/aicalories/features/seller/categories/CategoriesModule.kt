package com.sirelon.aicalories.features.seller.categories

import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.categories.domain.CategoriesMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val categoriesModule = module {
    factoryOf(::CategoriesMapper)
    factoryOf(::CategoriesRepository)
}