package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * A link that opens Google Maps navigating the route: the user's location to its first waypoint, then on to the destination.
 */
fun googleMapsLink(route: Route): String = buildString {
    append("https://www.google.com/maps/dir/?api=1")
    append("&destination=").append(route.destination.location.coordinates())
    val waypoints = route.handOff.waypoints
    if (waypoints.isNotEmpty()) {
        // Google separates waypoints with an encoded pipe
        append("&waypoints=").append(waypoints.joinToString(separator = "%7C") { it.coordinates() })
    }
    append("&travelmode=driving&dir_action=navigate")
}

/**
 * A link that opens Apple Maps with directions along the route: the user's location to its first waypoint, then on to the
 * destination. Apple Maps links can't start navigation; the user taps Go.
 */
fun appleMapsLink(route: Route): String = buildString {
    append("https://maps.apple.com/directions?destination=").append(route.destination.location.coordinates())
    route.handOff.waypoints.forEach { append("&waypoint=").append(it.coordinates()) }
    append("&mode=driving")
}

private fun GeoPoint.coordinates(): String = "${latitude.toLinkDegrees()},${longitude.toLinkDegrees()}"

// Six decimals is about 0.1 m; the integer is rounded first, so the text is the same for the same value on every platform.
private const val MICRODEGREES_PER_DEGREE = 1_000_000L
private const val MICRODEGREES_DECIMALS = 6

private fun Double.toLinkDegrees(): String {
    val microdegrees = (this * MICRODEGREES_PER_DEGREE).roundToLong()
    val sign = if (microdegrees < 0) "-" else ""
    val absolute = abs(microdegrees)
    val whole = absolute / MICRODEGREES_PER_DEGREE
    val fraction = (absolute % MICRODEGREES_PER_DEGREE).toString().padStart(MICRODEGREES_DECIMALS, '0').trimEnd('0')
    return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
}
