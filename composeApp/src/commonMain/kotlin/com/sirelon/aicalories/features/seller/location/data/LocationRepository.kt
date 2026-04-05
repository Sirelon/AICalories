package com.sirelon.aicalories.features.seller.location.data

import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.location.LocationProvider
import com.sirelon.aicalories.features.seller.location.OlxLocation

class LocationRepository(
    private val locationProvider: LocationProvider,
    private val olxApiClient: OlxApiClient,
) {
    suspend fun fetchUserLocation(): OlxLocation? {
        val deviceLocation = locationProvider.getCurrentLocation() ?: return null

        val locations = olxApiClient.getLocations(
            latitude = deviceLocation.latitude,
            longitude = deviceLocation.longitude,
        )

        val firstValidLocation = locations.firstNotNullOfOrNull { location ->
            val cityName = location.city?.name ?: return@firstNotNullOfOrNull null

            OlxLocation(
                cityName = cityName,
                districtName = location.district?.name,
            )
        }

        return firstValidLocation
    }
}
