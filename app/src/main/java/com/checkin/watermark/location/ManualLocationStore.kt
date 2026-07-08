package com.checkin.watermark.location

import android.content.Context

class ManualLocationStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences("manual-location", Context.MODE_PRIVATE)

    fun readLocationName(): String =
        preferences.getString(KEY_LOCATION_NAME, null).orEmpty()

    fun saveLocationName(name: String) {
        preferences.edit().putString(KEY_LOCATION_NAME, name.trim()).apply()
    }

    private companion object {
        const val KEY_LOCATION_NAME = "location_name"
    }
}
