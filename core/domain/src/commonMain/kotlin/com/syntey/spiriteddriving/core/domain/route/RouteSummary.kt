package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlin.time.Duration

/**
 * A route without its line and hand-off, as lists show it.
 *
 * @property id Saving the route or recording a drive on it refers to it by this ID.
 * @property funLevel How fun the route is, from 1 to 5, on one scale for every route, the fastest included.
 */
data class RouteSummary(
    val id: String,
    val start: Place,
    val destination: Place,
    val duration: Duration,
    val distance: Length,
    val funLevel: Int,
    val stats: RouteStats,
)
