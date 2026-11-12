package com.syntey.spiriteddriving.core.domain.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeoPolygonTest {

    private val square = listOf(
        GeoPoint(latitude = 0.0, longitude = 0.0),
        GeoPoint(latitude = 0.0, longitude = 10.0),
        GeoPoint(latitude = 10.0, longitude = 10.0),
        GeoPoint(latitude = 10.0, longitude = 0.0),
        GeoPoint(latitude = 0.0, longitude = 0.0),
    )

    @Test
    fun `a point inside the outline is contained`() {
        val polygon = GeoPolygon(outline = square)

        assertTrue(polygon.contains(GeoPoint(latitude = 5.0, longitude = 5.0)))
    }

    @Test
    fun `a point outside the outline is not contained`() {
        val polygon = GeoPolygon(outline = square)

        assertFalse(polygon.contains(GeoPoint(latitude = 5.0, longitude = 15.0)))
    }

    @Test
    fun `a point inside a hole is not contained`() {
        val hole = listOf(
            GeoPoint(latitude = 4.0, longitude = 4.0),
            GeoPoint(latitude = 4.0, longitude = 6.0),
            GeoPoint(latitude = 6.0, longitude = 6.0),
            GeoPoint(latitude = 6.0, longitude = 4.0),
            GeoPoint(latitude = 4.0, longitude = 4.0),
        )
        val polygon = GeoPolygon(outline = square, holes = listOf(hole))

        assertFalse(polygon.contains(GeoPoint(latitude = 5.0, longitude = 5.0)))
        assertTrue(polygon.contains(GeoPoint(latitude = 2.0, longitude = 2.0)))
    }

    @Test
    fun `a point in the notch of a concave outline is not contained`() {
        // A square with a notch cut in from the east side, between latitudes 4 and 6
        val concave = listOf(
            GeoPoint(latitude = 0.0, longitude = 0.0),
            GeoPoint(latitude = 0.0, longitude = 10.0),
            GeoPoint(latitude = 4.0, longitude = 10.0),
            GeoPoint(latitude = 4.0, longitude = 5.0),
            GeoPoint(latitude = 6.0, longitude = 5.0),
            GeoPoint(latitude = 6.0, longitude = 10.0),
            GeoPoint(latitude = 10.0, longitude = 10.0),
            GeoPoint(latitude = 10.0, longitude = 0.0),
            GeoPoint(latitude = 0.0, longitude = 0.0),
        )
        val polygon = GeoPolygon(outline = concave)

        assertFalse(polygon.contains(GeoPoint(latitude = 5.0, longitude = 8.0)))
        assertTrue(polygon.contains(GeoPoint(latitude = 2.0, longitude = 8.0)))
    }
}
