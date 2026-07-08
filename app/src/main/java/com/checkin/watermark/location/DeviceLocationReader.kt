package com.checkin.watermark.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.checkin.watermark.domain.Coordinate

data class DeviceLocationFix(
    val coordinate: Coordinate,
    val accuracyMeters: Float?,
)

class DeviceLocationReader(
    private val context: Context,
) {
    fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    fun readLastKnownLocation(): DeviceLocationFix? {
        if (!hasLocationPermission()) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)
            ?.let {
                it.toDeviceLocationFix()
            }
    }

    fun startLocationUpdates(
        onLocation: (DeviceLocationFix) -> Unit,
    ): LocationSubscription {
        if (!hasLocationPermission()) return LocationSubscription {}

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocation(location.toDeviceLocationFix())
            }

            @Deprecated("Deprecated in Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        manager.getProviders(true).forEach { provider ->
            runCatching {
                manager.requestLocationUpdates(provider, 2_000L, 3f, listener)
            }
        }

        return LocationSubscription {
            runCatching { manager.removeUpdates(listener) }
        }
    }

    private fun Location.toDeviceLocationFix(): DeviceLocationFix =
        DeviceLocationFix(
            coordinate = Coordinate(latitude, longitude),
            accuracyMeters = if (hasAccuracy()) accuracy else null,
        )
}

fun interface LocationSubscription {
    fun stop()
}
