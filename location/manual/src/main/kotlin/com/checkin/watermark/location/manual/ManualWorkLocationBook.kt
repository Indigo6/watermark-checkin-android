package com.checkin.watermark.location.manual

data class ManualWorkLocation(
    val id: String,
    val name: String,
)

data class ManualWorkLocationBook(
    val locations: List<ManualWorkLocation>,
    val selectedLocationId: String?,
) {
    fun selectedLocation(): ManualWorkLocation? =
        locations.firstOrNull { it.id == selectedLocationId } ?: locations.firstOrNull()

    fun add(name: String): ManualWorkLocationBook {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return this
        if (locations.any { it.name == trimmed }) return this
        val location = ManualWorkLocation(id = trimmed.stableId(), name = trimmed)
        return copy(
            locations = locations + location,
            selectedLocationId = selectedLocationId ?: location.id,
        )
    }

    fun select(id: String): ManualWorkLocationBook =
        if (locations.any { it.id == id }) copy(selectedLocationId = id) else this

    fun selectByName(name: String): ManualWorkLocationBook {
        val location = locations.firstOrNull { it.name == name.trim() } ?: return this
        return select(location.id)
    }

    fun deleteSelected(): ManualWorkLocationBook {
        val selected = selectedLocation() ?: return this
        val remaining = locations.filterNot { it.id == selected.id }
        return copy(
            locations = remaining,
            selectedLocationId = remaining.firstOrNull()?.id,
        )
    }

    companion object {
        fun empty(): ManualWorkLocationBook = ManualWorkLocationBook(emptyList(), null)
    }
}

private fun String.stableId(): String =
    lowercase()
        .trim()
        .replace(Regex("\\s+"), "-")
        .take(48)
