package com.checkin.watermark.location

import android.content.Context
import com.checkin.watermark.location.manual.ManualWorkLocation
import com.checkin.watermark.location.manual.ManualWorkLocationBook

class ManualLocationStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences("manual-location", Context.MODE_PRIVATE)

    fun readBook(): ManualWorkLocationBook {
        val encodedLocations = preferences.getString(KEY_LOCATIONS, null).orEmpty()
        val locations = encodedLocations
            .lineSequence()
            .mapNotNull { line ->
                val parts = line.split('\t', limit = 2)
                if (parts.size != 2) null else ManualWorkLocation(parts[0], parts[1])
            }
            .toList()
        val selectedId = preferences.getString(KEY_SELECTED_ID, null)
        val book = ManualWorkLocationBook(locations = locations, selectedLocationId = selectedId)
        if (book.locations.isNotEmpty()) return book

        val legacyName = preferences.getString(KEY_LOCATION_NAME, null).orEmpty()
        return ManualWorkLocationBook.empty().add(legacyName)
    }

    fun saveBook(book: ManualWorkLocationBook) {
        val encoded = book.locations.joinToString(separator = "\n") { location ->
            "${location.id}\t${location.name.replace('\t', ' ').replace('\n', ' ')}"
        }
        preferences.edit()
            .putString(KEY_LOCATIONS, encoded)
            .putString(KEY_SELECTED_ID, book.selectedLocation()?.id)
            .apply()
    }

    private companion object {
        const val KEY_LOCATION_NAME = "location_name"
        const val KEY_LOCATIONS = "locations"
        const val KEY_SELECTED_ID = "selected_location_id"
    }
}
