package com.checkin.watermark.location.manual

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSource
import org.junit.Assert.assertEquals
import org.junit.Test

class ManualLocationProviderTest {
    @Test
    fun `uses saved display name with current coordinate`() {
        val provider = ManualLocationProvider(savedLocationName = "北京朝阳某护理站")

        val snapshot = provider.resolve(
            coordinate = Coordinate(39.9219, 116.4431),
            accuracyMeters = 25f,
        )

        assertEquals("北京朝阳某护理站", snapshot.displayName)
        assertEquals(LocationSource.Manual, snapshot.source)
        assertEquals(25f, snapshot.accuracyMeters)
    }

    @Test
    fun `falls back to coordinate when no manual location exists`() {
        val provider = ManualLocationProvider(savedLocationName = "")

        val snapshot = provider.resolve(
            coordinate = Coordinate(39.9219, 116.4431),
            accuracyMeters = null,
        )

        assertEquals("GPS 39.921900, 116.443100", snapshot.displayName)
        assertEquals(LocationSource.CoordinateFallback, snapshot.source)
    }
}
