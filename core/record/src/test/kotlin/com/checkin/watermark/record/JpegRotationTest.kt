package com.checkin.watermark.record

import org.junit.Assert.assertEquals
import org.junit.Test

class JpegRotationTest {
    @Test
    fun `maps exif orientation constants to clockwise degrees`() {
        assertEquals(0, JpegRotation.degreesForExifOrientation(1))
        assertEquals(90, JpegRotation.degreesForExifOrientation(6))
        assertEquals(180, JpegRotation.degreesForExifOrientation(3))
        assertEquals(270, JpegRotation.degreesForExifOrientation(8))
    }

    @Test
    fun `uses zero degrees for unknown orientation`() {
        assertEquals(0, JpegRotation.degreesForExifOrientation(0))
        assertEquals(0, JpegRotation.degreesForExifOrientation(99))
    }
}
