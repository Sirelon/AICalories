package com.sirelon.aicalories.features.seller.categories.domain

import com.sirelon.aicalories.features.seller.categories.data.OlxCategoryResponse

class CategoriesMapper {

    fun mapCategory(response: OlxCategoryResponse): OlxCategory = OlxCategory(
        id = response.id,
        label = response.label.orEmpty(),
        parentId = response.parentId?.takeIf { it > 0 },
        isLeaf = response.isLeaf == true,
    )

}