package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * The drive the app may still ask about: the newest drive, on a fun route, until the user answers, skips or dismisses the
 * question. It keeps what the question shows, so it can be asked after the app restarts.
 *
 * @property routeNumber The route's number in the list it was picked from, which gives its badge and colour.
 * @property duration The route's planned duration.
 * @property line The route's line, from the start to the destination.
 * @property attribution Attribution for the route data, shown with the route.
 */
data class DriveQuestion(
    val driveId: String,
    val startedAt: Instant,
    val routeNumber: Int,
    val start: GeoPoint,
    val destination: Place,
    val duration: Duration,
    val distance: Length,
    val line: List<GeoPoint>,
    val attribution: String,
)
