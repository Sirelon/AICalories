package com.sirelon.aicalories.features.seller.openai.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIGeneratedAd(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("suggestedPrice")
    val suggestedPrice: Float,
    @SerialName("minPrice")
    val minPrice: Float,
    @SerialName("maxPrice")
    val maxPrice: Float,
)

