package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess

/**
 * What the map shows. Each platform's map draws it: Google Maps on Android, MapKit on iOS.
 *
 * @property showsUserLocation The user's location dot, shown with location access.
 * @property followsUser The map keeps the user in its middle as they move. Panning the map stops it.
 * @property userLocation Where the user is, once known.
 * @property overview The area the map shows when it doesn't follow the user, as without location access: the covered area's bounds,
 *   and Czechia's corners until the covered area is loaded.
 * @property coveredArea The covered area, dimmed outside and with a border along its edge; empty until it's loaded.
 * @property overviewRequest Grows each time the map should move to [overview], even when [overview] itself is unchanged, as when
 *   the user turns out to be outside the covered area.
 * @property destination The chosen destination's pin.
 */
data class RouteMapState(
    val showsUserLocation: Boolean = false,
    val followsUser: Boolean = false,
    val userLocation: GeoPoint? = null,
    val overview: GeoBounds = CZECHIA,
    val coveredArea: List<GeoPolygon> = emptyList(),
    val overviewRequest: Int = 0,
    val destination: GeoPoint? = null,
) {
    /**
     * The map once location access is [access]: it starts following the user when access is turned on, and stops,
     * forgetting where they were, when it's turned off.
     */
    fun withAccess(access: LocationAccess): RouteMapState {
        val allows = access.allowsLocation
        return copy(
            showsUserLocation = allows,
            followsUser = allows && (followsUser || !showsUserLocation),
            userLocation = userLocation.takeIf { allows },
        )
    }

    /**
     * Whether [point] is in the covered area. Until the covered area is loaded every point counts as covered, as recent places
     * were covered when they were chosen.
     */
    fun covers(point: GeoPoint): Boolean = coveredArea.isEmpty() || coveredArea.any { it.contains(point) }

    /** Whether the user is outside the covered area; false until both are known. */
    val isOutsideCoveredArea: Boolean
        get() {
            val location = userLocation ?: return false
            return coveredArea.isNotEmpty() && coveredArea.none { it.contains(location) }
        }

    /**
     * The map once the covered area is [coverage]. The first one becomes the overview; a later one is only redrawn, so the
     * camera doesn't move.
     */
    fun withCoverage(coverage: Coverage): RouteMapState =
        copy(
            coveredArea = coverage.area,
            overview = if (coveredArea.isEmpty()) coverage.bounds else overview,
        ).showingOutside(previous = this)

    /** The map once the user is at [location]. */
    fun withUserLocation(location: GeoPoint): RouteMapState = copy(userLocation = location).showingOutside(previous = this)

    /** The map with a pin at [point]: it stops following the user and moves to the streets around the pin. */
    fun withDestination(point: GeoPoint): RouteMapState = copy(
        destination = point,
        followsUser = false,
        overview = GeoBounds(
            southWest = GeoPoint(latitude = point.latitude - DESTINATION_MARGIN_LAT, longitude = point.longitude - DESTINATION_MARGIN_LON),
            northEast = GeoPoint(latitude = point.latitude + DESTINATION_MARGIN_LAT, longitude = point.longitude + DESTINATION_MARGIN_LON),
        ),
        overviewRequest = overviewRequest + 1,
    )

    /**
     * The map with a pin at [point], picked on the map: it stops following the user, and the camera stays, as the user is already
     * looking there.
     */
    fun withPickedPoint(point: GeoPoint): RouteMapState = copy(destination = point, followsUser = false)

    // The first time both the covered area and the user's location are known and the user is outside, the map stops following
    // and shows both. Later locations don't move it again.
    private fun showingOutside(previous: RouteMapState): RouteMapState {
        val location = userLocation
        val knewBoth = previous.userLocation != null && previous.coveredArea.isNotEmpty()
        if (knewBoth || location == null || !isOutsideCoveredArea) return this
        return copy(
            followsUser = false,
            overview = overview.including(location),
            overviewRequest = overviewRequest + 1,
        )
    }

    private companion object {
        // About a kilometre each way around a destination's pin
        const val DESTINATION_MARGIN_LAT = 0.01
        const val DESTINATION_MARGIN_LON = 0.015

        // The launch region, by its corners
        @Suppress("MagicNumber")
        val CZECHIA =
            GeoBounds(southWest = GeoPoint(latitude = 48.55, longitude = 12.09), northEast = GeoPoint(latitude = 51.06, longitude = 18.86))
    }
}
