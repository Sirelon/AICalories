package com.sirelon.aicalories.features.seller.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager

actual fun createLocationProvider(): LocationProvider {
    val context = AndroidAppContextHolder.applicationContext ?: return EmptyLocationProvider
    return AndroidLocationProvider(context)
}

private object EmptyLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): DeviceLocation? = null
}

class AndroidLocationProvider(
    private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): DeviceLocation? {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )

        for (provider in providers) {
            if (!locationManager.isProviderEnabled(provider)) continue
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                return DeviceLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
        }

        return null
    }
}
