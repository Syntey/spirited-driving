package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess

/**
 * What the map shows. Each platform's map draws it: Google Maps on Android, MapKit on iOS.
 *
 * @property showsUserLocation The user's location dot, shown with location access.
 * @property followsUser The map keeps the user in its middle as they move. Panning the map stops it.
 * @property userLocation Where the user is, once known.
 * @property overview The area the map shows when it doesn't follow the user, as without location access: the covered area.
 */
data class RouteMapState(
    val showsUserLocation: Boolean = false,
    val followsUser: Boolean = false,
    val userLocation: GeoPoint? = null,
    val overview: GeoBounds = CZECHIA,
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

    private companion object {
        // The launch region, by its corners
        @Suppress("MagicNumber")
        val CZECHIA =
            GeoBounds(southWest = GeoPoint(latitude = 48.55, longitude = 12.09), northEast = GeoPoint(latitude = 51.06, longitude = 18.86))
    }
}
