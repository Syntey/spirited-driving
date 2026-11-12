package com.syntey.spiriteddriving.core.domain.geo

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A point on the Earth, in WGS 84 degrees. */
data class GeoPoint(val latitude: Double, val longitude: Double) {

    /** The straight-line distance to [other] along the Earth's surface. */
    fun distanceTo(other: GeoPoint): Length {
        val lat1 = latitude.toRadians()
        val lat2 = other.latitude.toRadians()
        val deltaLat = lat2 - lat1
        val deltaLon = (other.longitude - longitude).toRadians()
        val a = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
        return (2 * EARTH_RADIUS_METRES * asin(sqrt(a))).metres
    }

    private fun Double.toRadians(): Double = this * PI / DEGREES_PER_HALF_TURN

    private companion object {
        const val EARTH_RADIUS_METRES = 6_371_000.0
        const val DEGREES_PER_HALF_TURN = 180.0
    }
}
