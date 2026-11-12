package com.syntey.spiriteddriving.core.domain.geo

/**
 * An area on the map: its outline, and any holes cut out of it. Each ring repeats its first point at the end, as GeoJSON does.
 */
data class GeoPolygon(val outline: List<GeoPoint>, val holes: List<List<GeoPoint>> = emptyList()) {

    /** Whether [point] is inside the outline and outside every hole. */
    fun contains(point: GeoPoint): Boolean = outline.encloses(point) && holes.none { it.encloses(point) }
}

/**
 * Even-odd ray casting over latitude and longitude as plane coordinates. Antimeridian crossings are not handled, since
 * the covered area doesn't span it.
 */
private fun List<GeoPoint>.encloses(point: GeoPoint): Boolean {
    var inside = false
    var previous = last()
    for (current in this) {
        val crossesLatitude = (current.latitude > point.latitude) != (previous.latitude > point.latitude)
        if (crossesLatitude) {
            val longitudeAtLatitude = previous.longitude +
                (point.latitude - previous.latitude) * (current.longitude - previous.longitude) / (current.latitude - previous.latitude)
            if (point.longitude < longitudeAtLatitude) inside = !inside
        }
        previous = current
    }
    return inside
}
