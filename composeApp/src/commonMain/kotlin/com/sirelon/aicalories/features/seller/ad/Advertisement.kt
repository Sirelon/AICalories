package com.sirelon.aicalories.features.seller.ad

import kotlinx.serialization.Serializable

@Serializable
data class Advertisement(
    val title: String,
    val description: String,
    val images: List<String>,
    val suggestedPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val condition: AdCondition,
)

enum class AdCondition {
    NEW, LIKE_NEW, GOOD, FAIR, POOR
}
