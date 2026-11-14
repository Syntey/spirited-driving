package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/**
 * A place a trip can start or end at.
 *
 * @property area Where it is, shown under its name, such as the town and region; null when there's none.
 */
data class Place(val name: String, val area: String?, val location: GeoPoint) {

    /** Whether [other] is the same place: the same name within 100 m, as when the backend moves its point a little. */
    fun isSameAs(other: Place): Boolean = name == other.name && location.distanceTo(other.location) <= SAME_PLACE_METRES

    private companion object {
        const val SAME_PLACE_METRES = 100.0
    }
}
