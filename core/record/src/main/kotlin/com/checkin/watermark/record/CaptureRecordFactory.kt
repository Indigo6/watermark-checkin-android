package com.checkin.watermark.record

import com.checkin.watermark.domain.LocationSnapshot
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object CaptureRecordFactory {
    private val idTimeFormatter = DateTimeFormatter
        .ofPattern("yyyyMMdd'T'HHmmss'Z'")
        .withZone(ZoneOffset.UTC)

    fun create(
        capturedAt: Instant,
        location: LocationSnapshot,
        originalBytes: ByteArray,
        outputBytes: ByteArray,
    ): CaptureRecord {
        val originalHash = Sha256Hasher.hash(originalBytes)
        val outputHash = Sha256Hasher.hash(outputBytes)
        return CaptureRecord(
            id = "${idTimeFormatter.format(capturedAt)}-${outputHash.take(8)}",
            capturedAt = capturedAt,
            location = location,
            originalSha256 = originalHash,
            outputSha256 = outputHash,
        )
    }
}
