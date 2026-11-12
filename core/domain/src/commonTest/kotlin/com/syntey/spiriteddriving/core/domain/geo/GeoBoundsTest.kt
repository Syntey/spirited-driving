package com.syntey.spiriteddriving.core.domain.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoBoundsTest {

    private val bounds = GeoBounds(
        southWest = GeoPoint(latitude = 0.0, longitude = 0.0),
        northEast = GeoPoint(latitude = 10.0, longitude = 10.0),
    )

    @Test
    fun `a point inside leaves the bounds equal`() {
        assertEquals(bounds, bounds.including(GeoPoint(latitude = 5.0, longitude = 5.0)))
    }

    @Test
    fun `a point north-west of the bounds grows only the north and west sides`() {
        assertEquals(
            GeoBounds(
                southWest = GeoPoint(latitude = 0.0, longitude = -5.0),
                northEast = GeoPoint(latitude = 20.0, longitude = 10.0),
            ),
            bounds.including(GeoPoint(latitude = 20.0, longitude = -5.0)),
        )
    }
}
