package com.sirelon.aicalories.features.seller.ad.data

import com.sirelon.aicalories.features.seller.ad.AdCondition
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.network.responses.Condition
import com.sirelon.aicalories.network.responses.GeneratedAd

class GeneratedAdMapper {

    fun mapToDomain(generatedAd: GeneratedAd): Advertisement {
        return Advertisement(
            title = generatedAd.title,
            description = generatedAd.description,
            suggestedPrice = generatedAd.suggestedPrice,
            minPrice = generatedAd.minPrice,
            maxPrice = generatedAd.maxPrice,
            category = generatedAd.category,
            condition = generatedAd.condition.toDomain(),
        )
    }

    private fun Condition.toDomain(): AdCondition = when (this) {
        Condition.NEW -> AdCondition.NEW
        Condition.LIKE_NEW -> AdCondition.LIKE_NEW
        Condition.GOOD -> AdCondition.GOOD
        Condition.FAIR -> AdCondition.FAIR
        Condition.POOR -> AdCondition.POOR
    }
}
