package com.syntey.spiriteddriving.core.domain.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode

/**
 * The area routes may start and end in, and the countries with motorway vignettes in and around it, in the order Settings lists them.
 */
data class Coverage(val area: List<GeoPolygon>, val vignetteCountries: List<CountryCode>) {

    init {
        require(area.isNotEmpty()) { "A coverage needs at least one polygon" }
    }

    /** The smallest bounds around every outline. */
    val bounds: GeoBounds
        get() {
            val points = area.flatMap { it.outline }
            return GeoBounds(
                southWest = GeoPoint(latitude = points.minOf { it.latitude }, longitude = points.minOf { it.longitude }),
                northEast = GeoPoint(latitude = points.maxOf { it.latitude }, longitude = points.maxOf { it.longitude }),
            )
        }

    /** Whether [point] is in the area. */
    fun covers(point: GeoPoint): Boolean = area.any { it.contains(point) }
}
