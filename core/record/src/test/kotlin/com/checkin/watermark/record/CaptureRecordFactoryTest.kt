package com.checkin.watermark.record

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSnapshot
import com.checkin.watermark.domain.LocationSource
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class CaptureRecordFactoryTest {
    @Test
    fun `creates capture record with hashes and metadata`() {
        val location = LocationSnapshot(
            displayName = "北京朝阳某护理站",
            coordinate = Coordinate(39.9219, 116.4431),
            accuracyMeters = 25f,
            source = LocationSource.Manual,
        )

        val record = CaptureRecordFactory.create(
            capturedAt = Instant.parse("2026-07-08T02:30:00Z"),
            location = location,
            originalBytes = "original".toByteArray(),
            outputBytes = "output".toByteArray(),
        )

        assertEquals("20260708T023000Z-e0ee8bb5", record.id)
        assertEquals(location, record.location)
        assertEquals("0682c5f2076f099c34cfdd15a9e063849ed437a49677e6fcc5b4198c76575be5", record.originalSha256)
        assertEquals("e0ee8bb50685e05fa0f47ed04203ae953fdfd055f5bd2892ea186504254f8c3a", record.outputSha256)
    }
}
