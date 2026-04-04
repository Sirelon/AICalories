package com.sirelon.aicalories.features.seller.categories.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class OlxCategorySuggestionResponse(
    @SerialName("a")
    val a: String?
)