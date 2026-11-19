package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A drive from the user's trip history, without its route's line and hand-off, for lists. Its full route comes from
 * [TripHistorySource.getRoute].
 *
 * @property startedAt When the route was handed off to the maps app.
 * @property routeId The ID of the [Route] that was driven.
 * @property kind Whether the route was the fastest one of its plan or a fun route.
 * @property funLevel How fun the route is, from 1 to 5.
 * @property rating The drive's own rating; null while it isn't rated.
 */
data class PastDrive(
    val id: String,
    val startedAt: Instant,
    val routeId: String,
    val kind: RouteKind,
    val start: Place,
    val destination: Place,
    val duration: Duration,
    val distance: Length,
    val funLevel: Int,
    val rating: Rating? = null,
)
