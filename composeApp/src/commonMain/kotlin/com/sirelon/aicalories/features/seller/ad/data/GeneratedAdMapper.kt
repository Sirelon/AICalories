package com.sirelon.aicalories.features.seller.ad.data

import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.network.responses.GeneratedAd

class GeneratedAdMapper {

    fun mapToDomain(generatedAd: GeneratedAd, images: List<String>): Advertisement {
        val normalizedMinPrice = minOf(
            generatedAd.minPrice,
            generatedAd.maxPrice,
            generatedAd.suggestedPrice,
        ).coerceAtLeast(0f)

        val normalizedMaxPrice = maxOf(
            generatedAd.minPrice,
            generatedAd.maxPrice,
            generatedAd.suggestedPrice,
        ).coerceAtLeast(normalizedMinPrice)

        val normalizedSuggestedPrice = generatedAd.suggestedPrice
            .coerceAtLeast(0f)
            .coerceIn(normalizedMinPrice, normalizedMaxPrice)

        return Advertisement(
            title = generatedAd.title.trim().ifBlank { "Товар" },
            description = generatedAd.description.trim().ifBlank { "Стан і деталі дивіться на фото." },
            suggestedPrice = normalizedSuggestedPrice,
            minPrice = normalizedMinPrice,
            maxPrice = normalizedMaxPrice,
            images = images.distinct(),
        )
    }
}
