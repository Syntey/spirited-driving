package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess

/**
 * @property locationAccess What the user allows; null until it's read on start.
 */
data class TripPlanViewState(val locationAccess: LocationAccess? = null, val map: RouteMapState = RouteMapState()) {
    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)
}
