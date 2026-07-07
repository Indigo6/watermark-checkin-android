package com.checkin.watermark.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.checkin.watermark.domain.Coordinate

data class DeviceLocationFix(
    val coordinate: Coordinate,
    val accuracyMeters: Float?,
)

class DeviceLocationReader(
    private val context: Context,
) {
    fun readLastKnownLocation(): DeviceLocationFix? {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)
            ?.let {
                DeviceLocationFix(
                    coordinate = Coordinate(it.latitude, it.longitude),
                    accuracyMeters = if (it.hasAccuracy()) it.accuracy else null,
                )
            }
    }
}
