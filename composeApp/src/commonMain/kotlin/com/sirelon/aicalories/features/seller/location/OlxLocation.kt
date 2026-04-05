package com.sirelon.aicalories.features.seller.location

data class OlxLocation(
    val cityName: String,
    val districtName: String?,
) {
    val displayName: String
        get() = if (districtName != null) "$cityName, $districtName" else cityName
}
