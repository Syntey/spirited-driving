package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class LineMidpointTest {

    @Test
    fun `a single point is its own midpoint`() {
        val point = GeoPoint(latitude = 50.0, longitude = 14.0)

        assertEquals(point, midpointAlong(listOf(point)))
    }

    @Test
    fun `the midpoint is the first point at or past half the length`() {
        // The first segment is 0.4 degrees of a 0.5 line, past half
        val points = listOf(at(0.0), at(0.4), at(0.5))

        assertEquals(at(0.4), midpointAlong(points))
    }

    @Test
    fun `the midpoint is measured along the line not by point count`() {
        // Three points, but the second is only a tenth of the way, so the middle point isn't halfway
        val points = listOf(at(0.0), at(0.1), at(-0.3))

        assertEquals(at(-0.3), midpointAlong(points))
    }

    @Test
    fun `a two point line takes its end`() {
        assertEquals(at(0.1), midpointAlong(listOf(at(0.0), at(0.1))))
    }

    private fun at(longitude: Double) = GeoPoint(latitude = 50.0, longitude = longitude)
}
