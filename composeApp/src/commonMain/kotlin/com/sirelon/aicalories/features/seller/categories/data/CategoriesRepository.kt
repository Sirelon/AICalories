package com.sirelon.aicalories.features.seller.categories.data

import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.categories.domain.CategoriesMapper
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEmpty

class CategoriesRepository(
    private val olxApiClient: OlxApiClient,
    private val mapper: CategoriesMapper,
) {

    private val categoriesFlow = emptyFlow<List<OlxCategory>>()
        .onEmpty {
            val result = olxApiClient.loadCategories()
            val data = result.map(mapper::mapCategory)
            emit(data)
        }


    fun loadCategories(): Flow<List<OlxCategory>> = categoriesFlow

}