package com.checkin.watermark.location.common

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSnapshot

interface ResolvedLocationProvider {
    fun resolve(
        coordinate: Coordinate?,
        accuracyMeters: Float?,
    ): LocationSnapshot
}
