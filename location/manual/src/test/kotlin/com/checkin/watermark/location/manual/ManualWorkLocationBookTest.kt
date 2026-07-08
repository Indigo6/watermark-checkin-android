package com.checkin.watermark.location.manual

import org.junit.Assert.assertEquals
import org.junit.Test

class ManualWorkLocationBookTest {
    @Test
    fun `adds first location and selects it`() {
        val book = ManualWorkLocationBook.empty()
            .add("北京朝阳某护理站")

        assertEquals("北京朝阳某护理站", book.selectedLocation()?.name)
        assertEquals(1, book.locations.size)
    }

    @Test
    fun `adds multiple locations and switches selection`() {
        val book = ManualWorkLocationBook.empty()
            .add("北京朝阳某护理站")
            .add("上海浦东某养老院")
        val selected = book.select(book.locations[0].id)

        assertEquals("北京朝阳某护理站", selected.selectedLocation()?.name)
        assertEquals(2, selected.locations.size)
    }

    @Test
    fun `ignores blank and duplicate names`() {
        val book = ManualWorkLocationBook.empty()
            .add("北京朝阳某护理站")
            .add(" ")
            .add(" 北京朝阳某护理站 ")

        assertEquals(1, book.locations.size)
    }

    @Test
    fun `deleting selected location selects next available location`() {
        val book = ManualWorkLocationBook.empty()
            .add("地点一")
            .add("地点二")
            .selectByName("地点一")
            .deleteSelected()

        assertEquals("地点二", book.selectedLocation()?.name)
        assertEquals(1, book.locations.size)
    }
}
