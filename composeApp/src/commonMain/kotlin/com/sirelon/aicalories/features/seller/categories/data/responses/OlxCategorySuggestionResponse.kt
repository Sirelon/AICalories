package com.sirelon.aicalories.features.seller.categories.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class OlxCategorySuggestionResponse(
    @SerialName("data")
    val data: List<OlxCategorySuggestionResponseItem>
) {
    @Serializable
    internal class OlxCategorySuggestionResponseItem(@SerialName("id") val id: Int)
}

