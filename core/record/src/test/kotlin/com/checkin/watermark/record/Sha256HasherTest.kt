package com.checkin.watermark.record

import org.junit.Assert.assertEquals
import org.junit.Test

class Sha256HasherTest {
    @Test
    fun `hashes bytes as lowercase sha256 hex`() {
        val hash = Sha256Hasher.hash("watermark".toByteArray())

        assertEquals(
            "4928dd49738cdfa9ac7eb4411152d4c5397d4238fb3097eeed5d0145053b0882",
            hash,
        )
    }
}
