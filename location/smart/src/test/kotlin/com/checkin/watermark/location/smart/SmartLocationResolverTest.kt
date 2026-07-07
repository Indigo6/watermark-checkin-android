package com.checkin.watermark.location.smart

import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSource
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartLocationResolverTest {
    @Test
    fun `uses nearest configured site before cache or backend`() {
        val resolver = SmartLocationResolver(
            sites = listOf(CheckinSite("site-1", "上海浦东某养老院", Coordinate(31.2304, 121.4737), radiusMeters = 150)),
            cache = InMemoryAddressCache(
                listOf(CachedAddress(Coordinate(31.2305, 121.4738), "缓存地址", maxDistanceMeters = 200)),
            ),
            backend = FakeReverseGeocoder("在线地址"),
        )

        val snapshot = resolver.resolve(Coordinate(31.23041, 121.47372), accuracyMeters = 18f)

        assertEquals("上海浦东某养老院", snapshot.displayName)
        assertEquals(LocationSource.ConfiguredSite, snapshot.source)
    }

    @Test
    fun `uses cache before backend`() {
        val resolver = SmartLocationResolver(
            sites = emptyList(),
            cache = InMemoryAddressCache(
                listOf(CachedAddress(Coordinate(31.2305, 121.4738), "缓存地址", maxDistanceMeters = 200)),
            ),
            backend = FakeReverseGeocoder("在线地址"),
        )

        val snapshot = resolver.resolve(Coordinate(31.23041, 121.47372), accuracyMeters = 18f)

        assertEquals("缓存地址", snapshot.displayName)
        assertEquals(LocationSource.CachedAddress, snapshot.source)
    }

    @Test
    fun `uses backend when no local match exists`() {
        val resolver = SmartLocationResolver(
            sites = emptyList(),
            cache = InMemoryAddressCache(emptyList()),
            backend = FakeReverseGeocoder("在线地址"),
        )

        val snapshot = resolver.resolve(Coordinate(31.23041, 121.47372), accuracyMeters = 18f)

        assertEquals("在线地址", snapshot.displayName)
        assertEquals(LocationSource.OnlineReverseGeocode, snapshot.source)
    }

    @Test
    fun `falls back to coordinate when backend fails`() {
        val resolver = SmartLocationResolver(
            sites = emptyList(),
            cache = InMemoryAddressCache(emptyList()),
            backend = FakeReverseGeocoder(null),
        )

        val snapshot = resolver.resolve(Coordinate(31.23041, 121.47372), accuracyMeters = 18f)

        assertEquals("GPS 31.230410, 121.473720", snapshot.displayName)
        assertEquals(LocationSource.CoordinateFallback, snapshot.source)
    }
}

private class FakeReverseGeocoder(
    private val address: String?,
) : ReverseGeocoder {
    override fun reverseGeocode(coordinate: Coordinate): String? = address
}
