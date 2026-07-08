package com.checkin.watermark.record

object JpegRotation {
    fun degreesForExifOrientation(orientation: Int): Int =
        when (orientation) {
            6 -> 90
            3 -> 180
            8 -> 270
            else -> 0
        }
}
