package com.checkin.watermark.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    fun display(): String = "%.6f, %.6f".format(latitude, longitude)

    fun distanceMetersTo(other: Coordinate): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(other.latitude)
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
}
