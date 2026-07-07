package com.checkin.watermark.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WatermarkTextBuilder(
    private val clockZone: ZoneId,
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun build(
        template: WatermarkTemplate,
        capturedAt: Instant,
        location: LocationSnapshot,
    ): List<String> {
        val displayLocation = location.displayName.ifBlank {
            location.coordinate?.let { "GPS ${it.display()}" } ?: "位置不可用"
        }
        return buildList {
            add(template.title)
            add(formatter.format(capturedAt.atZone(clockZone)))
            add(displayLocation)
            location.coordinate?.let { coordinate ->
                val accuracy = location.accuracyMeters?.let { " · 精度 ${it.roundToInt()}m" }.orEmpty()
                add("GPS ${coordinate.display()}$accuracy")
            }
        }.distinct()
    }
}
