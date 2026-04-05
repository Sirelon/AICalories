package com.sirelon.aicalories.network.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratedAd(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("suggestedPrice")
    val suggestedPrice: Double,
    @SerialName("minPrice")
    val minPrice: Double,
    @SerialName("maxPrice")
    val maxPrice: Double,
    @SerialName("condition")
    val condition: Condition,
)

enum class Condition {
    @SerialName("new")
    NEW,
    @SerialName("like_new")
    LIKE_NEW,
    @SerialName("good")
    GOOD,
    @SerialName("fair")
    FAIR,
    @SerialName("poor")
    POOR
}
