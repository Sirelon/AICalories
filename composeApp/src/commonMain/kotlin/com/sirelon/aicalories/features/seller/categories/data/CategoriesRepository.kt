package com.sirelon.aicalories.features.seller.categories.data

import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.categories.domain.CategoriesMapper
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val categoriesCacheMutex = Mutex()
    private var cachedCategories: List<OlxCategory>? = null

    fun loadCategories(): Flow<List<OlxCategory>> = flow {
        emit(getSupportedCategories())
    }

    fun getRootCategories(): Flow<List<OlxCategory>> =
        loadCategories().map { all -> all.filter { it.parentId == null } }

    fun getSubcategories(parentId: Int): Flow<List<OlxCategory>> =
        loadCategories().map { all -> all.filter { it.parentId == parentId } }

    suspend fun getCategoryById(id: Int): OlxCategory? =
        getSupportedCategories().find { it.id == id }

    fun getAttributes(categoryId: Int): Flow<List<OlxAttribute>> = flow {
        val response = olxApiClient.loadAttributes(categoryId)
        emit(mapper.mapAttributes(response))
    }

    fun categorySuggestion(title: String): Flow<OlxCategory> = flow {
        val response = olxApiClient.loadCategorySuggestionId(title)
        if (response == null) {
            emit(null)
        } else {
            emit(getCategoryById(response))
        }
    }
        .filterNotNull()

    private suspend fun loadSupportedCategories(): List<OlxCategory> {
        val result = olxApiClient.loadCategories()
        val data = result.mapNotNull(mapper::mapCategory)

        return normalize(data)
    }

    private suspend fun getSupportedCategories(): List<OlxCategory> =
        cachedCategories ?: categoriesCacheMutex.withLock {
            cachedCategories ?: loadSupportedCategories().also { cachedCategories = it }
        }

    private fun normalize(data: List<OlxCategory>): List<OlxCategory> {
        val grouppedData = data.groupBy { it.parentId }.toMutableMap()

        val rootCategories = grouppedData[null].orEmpty()
        val toRemove = rootCategories.filter { notSupportedParentIds.contains(it.id) }

        grouppedData[null] = rootCategories - toRemove

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
