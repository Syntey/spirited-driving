package com.syntey.spiriteddriving.core.domain.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PolylineTest {

    @Test
    fun `Google's documented example decodes to its three points`() {
        val points = decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@")

        assertEquals(3, points.size)
        assertPoint(GeoPoint(38.5, -120.2), points[0])
        assertPoint(GeoPoint(40.7, -120.95), points[1])
        assertPoint(GeoPoint(43.252, -126.453), points[2])
    }

    @Test
    fun `empty string decodes to no points`() {
        assertTrue(decodePolyline("").isEmpty())
    }

    @Test
    fun `a single point decodes to one point`() {
        val points = decodePolyline("_p~iF~ps|U")

        assertEquals(1, points.size)
        assertPoint(GeoPoint(38.5, -120.2), points.single())
    }

    private fun assertPoint(expected: GeoPoint, actual: GeoPoint) {
        assertEquals(expected.latitude, actual.latitude, absoluteTolerance = 1e-6)
        assertEquals(expected.longitude, actual.longitude, absoluteTolerance = 1e-6)
    }
}
