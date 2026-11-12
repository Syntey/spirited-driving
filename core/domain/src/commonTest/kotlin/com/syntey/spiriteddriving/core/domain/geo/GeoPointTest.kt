package com.syntey.spiriteddriving.core.domain.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoPointTest {

    @Test
    fun `a point is 0 metres from itself`() {
        val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)

        assertEquals(0.0, prague.distanceTo(prague).inMetres, absoluteTolerance = 1e-6)
    }

    @Test
    fun `Prague to Karlstejn is about 23 km`() {
        val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)
        val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)

        assertEquals(23392.0, prague.distanceTo(karlstejn).inMetres, absoluteTolerance = 1.0)
    }
}
