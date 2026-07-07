package com.checkin.watermark.record

import com.checkin.watermark.domain.LocationSnapshot
import java.time.Instant

data class CaptureRecord(
    val id: String,
    val capturedAt: Instant,
    val location: LocationSnapshot,
    val originalSha256: String,
    val outputSha256: String,
)
