package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import kotlin.test.Test
import kotlin.test.assertFalse
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

    private fun place(name: String, latitude: Double) =
        Place(name = name, area = null, location = GeoPoint(latitude = latitude, longitude = 14.1832917))
}
