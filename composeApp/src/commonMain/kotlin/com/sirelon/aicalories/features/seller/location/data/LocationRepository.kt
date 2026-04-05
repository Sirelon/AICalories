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

        val first = locations.firstOrNull() ?: return null
        val cityName = first.city?.name ?: return null

        return OlxLocation(
            cityName = cityName,
            districtName = first.district?.name,
        )
    }
}
