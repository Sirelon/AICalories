package com.sirelon.aicalories.features.seller.location

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
)

interface LocationProvider {
    suspend fun getCurrentLocation(): DeviceLocation?
}

expect fun createLocationProvider(): LocationProvider
