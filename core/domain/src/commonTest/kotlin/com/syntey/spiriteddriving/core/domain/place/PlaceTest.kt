package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaceTest {

    @Test
    fun `the same name about 50 m away is the same place`() {
        assertTrue(place("Karlštejn", 49.9338).isSameAs(place("Karlštejn", 49.9338 + 0.00045)))
    }

    @Test
    fun `the same name about 200 m away is another place`() {
        assertFalse(place("Karlštejn", 49.9338).isSameAs(place("Karlštejn", 49.9338 + 0.0018)))
    }

    @Test
    fun `a different name at the same point is another place`() {
        assertFalse(place("Karlštejn", 49.9338).isSameAs(place("Karlštejnská", 49.9338)))
    }

    @Test
    fun `the short area is the first part of the area`() {
        assertEquals("Karlštejn", Place(name = "Karlštejn Castle", area = "Karlštejn, Central Bohemia", location = POINT).shortArea)
    }

    @Test
    fun `the short area is the name when the first part of the area already holds it`() {
        assertEquals(
            "Beroun",
            Place(name = "Beroun", area = "okres Beroun, Středočeský kraj", location = POINT).shortArea,
        )
    }

    @Test
    fun `the short area of a place without an area is its name`() {
        assertEquals("Vinohradská 12", Place(name = "Vinohradská 12", area = null, location = POINT).shortArea)
    }

    @Test
    fun `a place without an area or a name has no short area`() {
        assertNull(Place(name = null, area = null, location = POINT).shortArea)
    }

    private fun place(name: String, latitude: Double) =
        Place(name = name, area = null, location = GeoPoint(latitude = latitude, longitude = 14.1832917))

    private companion object {
        val POINT = GeoPoint(latitude = 49.9338, longitude = 14.1832917)
    }
}
