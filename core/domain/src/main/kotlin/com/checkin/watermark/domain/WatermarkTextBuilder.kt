package com.checkin.watermark.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WatermarkTextBuilder(
    private val clockZone: ZoneId,
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val minimalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun build(
        template: WatermarkTemplate,
        capturedAt: Instant,
        location: LocationSnapshot,
    ): List<String> {
        val displayLocation = location.displayName.ifBlank {
            location.coordinate?.let { "GPS ${it.display()}" } ?: "位置不可用"
        }
        if (template == WatermarkTemplate.MinimalCamera) {
            val capturedDateTime = capturedAt.atZone(clockZone)
            val formattedTime = minimalFormatter.format(capturedDateTime)
            return buildList {
                add("${formattedTime.substring(0, 10)} ${capturedDateTime.dayOfWeek.chineseName()} ${formattedTime.substring(11)}")
                add(displayLocation)
                location.coordinate?.let { coordinate ->
                    add("GPS ${coordinate.display()}")
                }
            }.distinct()
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

    private fun java.time.DayOfWeek.chineseName(): String = when (this) {
        java.time.DayOfWeek.MONDAY -> "周一"
        java.time.DayOfWeek.TUESDAY -> "周二"
        java.time.DayOfWeek.WEDNESDAY -> "周三"
        java.time.DayOfWeek.THURSDAY -> "周四"
        java.time.DayOfWeek.FRIDAY -> "周五"
        java.time.DayOfWeek.SATURDAY -> "周六"
        java.time.DayOfWeek.SUNDAY -> "周日"
    }
}
