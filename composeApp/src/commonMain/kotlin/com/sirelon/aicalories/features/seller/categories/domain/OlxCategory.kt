package com.sirelon.aicalories.features.seller.categories.domain

data class OlxCategory(
    val id: Int,
    val label: String,
    val parentId: Int?,
    val isLeaf: Boolean,
)