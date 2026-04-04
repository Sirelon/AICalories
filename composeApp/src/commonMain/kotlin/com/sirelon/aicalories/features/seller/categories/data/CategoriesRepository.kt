package com.sirelon.aicalories.features.seller.categories.data

import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.categories.domain.CategoriesMapper
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

class CategoriesRepository(
    private val olxApiClient: OlxApiClient,
    private val mapper: CategoriesMapper,
) {
    private val notSupportedParentIds = listOf(
        1, // нерухомість
        1532, // autotransport
        6, // work
        35, // тварини?? але під питанням, бо там є зоотовари
        7, // бізнесс і послуги
        3709, // житло подобово
        3428, // Оренда та прокат ?
    )

    private val categoriesFlow = emptyFlow<List<OlxCategory>>()
        .onEmpty {
            val data = loadSupportedCategories()
            emit(data)
        }

    fun loadCategories(): Flow<List<OlxCategory>> = categoriesFlow

    fun getRootCategories(): Flow<List<OlxCategory>> =
        categoriesFlow.map { all -> all.filter { it.parentId == null } }

    fun getSubcategories(parentId: Int): Flow<List<OlxCategory>> =
        categoriesFlow.map { all -> all.filter { it.parentId == parentId } }

    suspend fun getCategoryById(id: Int): OlxCategory? =
        categoriesFlow.first().find { it.id == id }

    private suspend fun loadSupportedCategories(): List<OlxCategory> {
        val result = olxApiClient.loadCategories()
        val data = result.map(mapper::mapCategory)

        return normalize(data)
    }

    private fun normalize(data: List<OlxCategory>): List<OlxCategory> {
        val grouppedData = data.groupBy { it.parentId }.toMutableMap()

        val toRemove = grouppedData[null].orEmpty().filter { notSupportedParentIds.contains(it.id) }

        grouppedData.removeLeaves(toRemove)

        return grouppedData
            .values
            .flatten()
            .toList()
    }

    private fun MutableMap<Int?, List<OlxCategory>>.removeLeaves(toRemove: List<OlxCategory>) {
        if (toRemove.isEmpty()) return

        val removeSub = toRemove
            .flatMap { this.remove(it.id).orEmpty() }

        removeLeaves(removeSub)
    }
}