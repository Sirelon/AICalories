package com.sirelon.aicalories.features.seller.ad.data

import com.sirelon.aicalories.features.seller.auth.data.AdvertContactRequest
import com.sirelon.aicalories.features.seller.auth.data.AdvertImageRequest
import com.sirelon.aicalories.features.seller.auth.data.AdvertLocationRequest
import com.sirelon.aicalories.features.seller.auth.data.AdvertPriceRequest
import com.sirelon.aicalories.features.seller.auth.data.PostAdvertRequest
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.OlxLocation

internal object PostAdvertRequestMapper {

    fun map(
        title: String,
        description: String,
        category: OlxCategory,
        location: OlxLocation,
        images: List<String>,
        price: Float,
        contactName: String,
    ): PostAdvertRequest = PostAdvertRequest(
        title = title,
        description = description,
        categoryId = category.id,
        advertiserType = "private",
        contact = AdvertContactRequest(name = contactName, phone = null),
        location = AdvertLocationRequest(cityId = location.cityId, districtId = location.districtId),
        images = images.map { AdvertImageRequest(url = it) },
        price = AdvertPriceRequest(value = price.toInt(), currency = "UAH", negotiable = false),
        attributes = emptyList(), // TODO SIR-17: Implement attribute selection
    )
}
