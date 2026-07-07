package com.checkin.watermark.domain

data class LocationSnapshot(
    val displayName: String,
    val coordinate: Coordinate?,
    val accuracyMeters: Float?,
    val source: LocationSource,
)

enum class LocationSource {
    Manual,
    ConfiguredSite,
    CachedAddress,
    OnlineReverseGeocode,
    CoordinateFallback,
    Unavailable,
}
