package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A route the user saved, without its line and hand-off, for lists. Its full route comes from
 * [SavedRoutesSource.getRoute].
 *
 * @property id The saved route's own ID, not its route's.
 * @property routeId The ID of the [Route] it keeps, as planned.
 * @property funLevel How fun the route is, from 1 to 5.
 */
data class SavedRoute(
    val id: String,
    val name: String,
    val savedAt: Instant,
    val routeId: String,
    val start: Place,
    val destination: Place,
    val duration: Duration,
    val distance: Length,
    val funLevel: Int,
    val stats: RouteStats,
)
