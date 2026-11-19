package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres

/**
 * A place a trip can start or end at.
 *
 * @property name Its name; null for a point picked on the map that has no name, as no address is near it or its lookup failed.
 * @property area Where it is, shown under its name, such as the town and region; null when there's none.
 */
data class Place(val name: String?, val area: String?, val location: GeoPoint) {

    /** Whether [other] is the same place: the same name within 100 m, as when the backend moves its point a little. */
    fun isSameAs(other: Place): Boolean = name == other.name && location.distanceTo(other.location) <= SAME_PLACE_DISTANCE

    /**
     * The first part of [area], such as "Smíchov" of "Smíchov, Prague", for a short "From Smíchov"; the name without an
     * area, and null without either.
     */
    val shortArea: String?
        get() = area?.substringBefore(',')?.trim()?.ifEmpty { null } ?: name

    private companion object {
        val SAME_PLACE_DISTANCE: Length = 100.metres
    }
}
