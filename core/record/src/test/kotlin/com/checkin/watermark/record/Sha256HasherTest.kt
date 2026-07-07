package com.checkin.watermark.record

import org.junit.Assert.assertEquals
import org.junit.Test

class Sha256HasherTest {
    @Test
    fun `hashes bytes as lowercase sha256 hex`() {
        val hash = Sha256Hasher.hash("watermark".toByteArray())

        assertEquals(
            "ad5f5ad35d28a00a9cbe1ae157fcb3fccd65d873ef0ba67e0ff2dcf11b88a6bb",
            hash,
        )
    }
}
