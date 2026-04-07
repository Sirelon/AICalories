package com.sirelon.aicalories.features.seller.ad.data

import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.network.responses.GeneratedAd

class GeneratedAdMapper {

    fun mapToDomain(generatedAd: GeneratedAd, images: List<String>): Advertisement {
        return Advertisement(
            title = generatedAd.title,
            description = generatedAd.description,
            suggestedPrice = generatedAd.suggestedPrice,
            minPrice = generatedAd.minPrice,
            maxPrice = generatedAd.maxPrice,
            images = images,
        )
    }
}
