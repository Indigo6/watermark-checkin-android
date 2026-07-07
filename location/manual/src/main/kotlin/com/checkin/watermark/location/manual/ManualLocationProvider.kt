package com.checkin.watermark.location.manual

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSnapshot
import com.checkin.watermark.domain.LocationSource
import com.checkin.watermark.location.common.ResolvedLocationProvider

class ManualLocationProvider(
    private val savedLocationName: String,
) : ResolvedLocationProvider {
    override fun resolve(
        coordinate: Coordinate?,
        accuracyMeters: Float?,
    ): LocationSnapshot {
        val trimmed = savedLocationName.trim()
        return if (trimmed.isNotEmpty()) {
            LocationSnapshot(trimmed, coordinate, accuracyMeters, LocationSource.Manual)
        } else {
            LocationSnapshot(
                displayName = coordinate?.let { "GPS ${it.display()}" } ?: "位置不可用",
                coordinate = coordinate,
                accuracyMeters = accuracyMeters,
                source = if (coordinate == null) LocationSource.Unavailable else LocationSource.CoordinateFallback,
            )
        }
    }
}
