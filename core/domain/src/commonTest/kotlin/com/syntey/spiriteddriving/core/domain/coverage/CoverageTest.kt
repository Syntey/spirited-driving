package com.syntey.spiriteddriving.core.domain.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoverageTest {

    private val first = GeoPolygon(outline = square(south = 0.0, west = 0.0, size = 10.0))
    private val second = GeoPolygon(outline = square(south = 20.0, west = 30.0, size = 5.0))

    @Test
    fun `bounds span all polygons`() {
        val coverage = Coverage(area = listOf(first, second), vignetteCountries = emptyList())

        assertEquals(
            GeoBounds(
                southWest = GeoPoint(latitude = 0.0, longitude = 0.0),
                northEast = GeoPoint(latitude = 25.0, longitude = 35.0),
            ),
            coverage.bounds,
        )
    }

    @Test
    fun `covers a point in any polygon`() {
        val coverage = Coverage(area = listOf(first, second), vignetteCountries = emptyList())

        assertTrue(coverage.covers(GeoPoint(latitude = 22.0, longitude = 32.0)))
        assertFalse(coverage.covers(GeoPoint(latitude = 15.0, longitude = 15.0)))
    }

    @Test
    fun `an empty area is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Coverage(area = emptyList(), vignetteCountries = emptyList())
        }
    }

    private fun square(south: Double, west: Double, size: Double) = listOf(
        GeoPoint(latitude = south, longitude = west),
        GeoPoint(latitude = south, longitude = west + size),
        GeoPoint(latitude = south + size, longitude = west + size),
        GeoPoint(latitude = south + size, longitude = west),
        GeoPoint(latitude = south, longitude = west),
    )
}
