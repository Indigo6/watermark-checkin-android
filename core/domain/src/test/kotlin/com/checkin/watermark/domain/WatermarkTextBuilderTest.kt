package com.checkin.watermark.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class WatermarkTextBuilderTest {
    @Test
    fun `builds minimal camera watermark without title or accuracy`() {
        val snapshot = LocationSnapshot(
            displayName = "北京市朝阳区护理站",
            coordinate = Coordinate(latitude = 39.9219, longitude = 116.4431),
            accuracyMeters = 30f,
            source = LocationSource.Manual,
        )

        val lines = WatermarkTextBuilder(
            clockZone = ZoneId.of("Asia/Shanghai"),
        ).build(
            template = WatermarkTemplate.MinimalCamera,
            capturedAt = Instant.parse("2026-07-09T06:28:36Z"),
            location = snapshot,
        )

        assertEquals(
            listOf(
                "2026-07-09 周四 14:28:36",
                "北京市朝阳区护理站",
                "GPS 39.921900, 116.443100",
            ),
            lines,
        )
    }

    @Test
    fun `builds worker watermark with address coordinate and accuracy`() {
        val snapshot = LocationSnapshot(
            displayName = "上海市浦东新区某项目部",
            coordinate = Coordinate(latitude = 31.2304, longitude = 121.4737),
            accuracyMeters = 18f,
            source = LocationSource.Manual,
        )

        val lines = WatermarkTextBuilder(
            clockZone = ZoneId.of("Asia/Shanghai"),
        ).build(
            template = WatermarkTemplate.WorkCheckin,
            capturedAt = Instant.parse("2026-07-07T06:32:18Z"),
            location = snapshot,
        )

        assertEquals(
            listOf(
                "工作打卡",
                "2026-07-07 14:32:18",
                "上海市浦东新区某项目部",
                "GPS 31.230400, 121.473700 · 精度 18m",
            ),
            lines,
        )
    }

    @Test
    fun `uses coordinate fallback when location name is blank`() {
        val snapshot = LocationSnapshot(
            displayName = "",
            coordinate = Coordinate(latitude = 31.0, longitude = 121.0),
            accuracyMeters = null,
            source = LocationSource.CoordinateFallback,
        )

        val lines = WatermarkTextBuilder(ZoneId.of("Asia/Shanghai")).build(
            template = WatermarkTemplate.WorkCheckin,
            capturedAt = Instant.parse("2026-07-07T06:32:18Z"),
            location = snapshot,
        )

        assertEquals("GPS 31.000000, 121.000000", lines[2])
    }
}
