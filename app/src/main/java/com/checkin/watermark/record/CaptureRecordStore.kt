package com.checkin.watermark.record

import android.content.Context
import com.checkin.watermark.domain.LocationSource
import com.checkin.watermark.record.CaptureRecord

class CaptureRecordStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences("capture-records", Context.MODE_PRIVATE)

    fun append(record: CaptureRecord) {
        val existing = preferences.getString(KEY_RECORDS, null).orEmpty()
        val next = buildString {
            if (existing.isNotBlank()) {
                append(existing)
                append('\n')
            }
            append(record.toLine())
        }
        preferences.edit().putString(KEY_RECORDS, next).apply()
    }

    fun readLines(): List<String> =
        preferences.getString(KEY_RECORDS, null)
            .orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .toList()

    private fun CaptureRecord.toLine(): String {
        val coordinate = location.coordinate?.display().orEmpty()
        val accuracy = location.accuracyMeters?.toString().orEmpty()
        val source = when (location.source) {
            LocationSource.Manual -> "manual"
            LocationSource.ConfiguredSite -> "configured_site"
            LocationSource.CachedAddress -> "cached_address"
            LocationSource.OnlineReverseGeocode -> "online_reverse_geocode"
            LocationSource.CoordinateFallback -> "coordinate_fallback"
            LocationSource.Unavailable -> "unavailable"
        }
        return listOf(
            id,
            capturedAt.toString(),
            location.displayName,
            coordinate,
            accuracy,
            source,
            originalSha256,
            outputSha256,
        ).joinToString(separator = "\t") { it.replace('\t', ' ').replace('\n', ' ') }
    }

    private companion object {
        const val KEY_RECORDS = "records"
    }
}
