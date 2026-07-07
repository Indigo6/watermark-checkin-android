package com.checkin.watermark.location.smart

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSnapshot
import com.checkin.watermark.domain.LocationSource
import com.checkin.watermark.location.common.ResolvedLocationProvider

data class CheckinSite(
    val id: String,
    val name: String,
    val coordinate: Coordinate,
    val radiusMeters: Int,
)

data class CachedAddress(
    val coordinate: Coordinate,
    val address: String,
    val maxDistanceMeters: Int,
)

interface ReverseGeocoder {
    fun reverseGeocode(coordinate: Coordinate): String?
}

class InMemoryAddressCache(
    private val addresses: List<CachedAddress>,
) {
    fun find(coordinate: Coordinate): CachedAddress? =
        addresses.firstOrNull { coordinate.distanceMetersTo(it.coordinate) <= it.maxDistanceMeters }
}

class SmartLocationResolver(
    private val sites: List<CheckinSite>,
    private val cache: InMemoryAddressCache,
    private val backend: ReverseGeocoder,
) : ResolvedLocationProvider {
    override fun resolve(
        coordinate: Coordinate?,
        accuracyMeters: Float?,
    ): LocationSnapshot {
        if (coordinate == null) {
            return LocationSnapshot("位置不可用", null, null, LocationSource.Unavailable)
        }

        val site = sites.firstOrNull { coordinate.distanceMetersTo(it.coordinate) <= it.radiusMeters }
        if (site != null) {
            return LocationSnapshot(site.name, coordinate, accuracyMeters, LocationSource.ConfiguredSite)
        }

        val cached = cache.find(coordinate)
        if (cached != null) {
            return LocationSnapshot(cached.address, coordinate, accuracyMeters, LocationSource.CachedAddress)
        }

        val onlineAddress = backend.reverseGeocode(coordinate)?.takeIf { it.isNotBlank() }
        if (onlineAddress != null) {
            return LocationSnapshot(onlineAddress, coordinate, accuracyMeters, LocationSource.OnlineReverseGeocode)
        }

        return LocationSnapshot(
            displayName = "GPS ${coordinate.display()}",
            coordinate = coordinate,
            accuracyMeters = accuracyMeters,
            source = LocationSource.CoordinateFallback,
        )
    }
}
