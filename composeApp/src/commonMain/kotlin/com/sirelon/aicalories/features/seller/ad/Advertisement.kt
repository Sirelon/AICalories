package com.sirelon.aicalories.features.seller.ad

data class Advertisement(
    val title: String,
    val description: String,
    val suggestedPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val category: String,
    val condition: AdCondition,
)

enum class AdCondition {
    NEW, LIKE_NEW, GOOD, FAIR, POOR
}
