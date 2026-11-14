package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlin.time.Duration

/** Whether a route is the fastest one of its plan or one of the fun routes. */
enum class RouteKind { Fastest, Fun }

/**
 * Facts about a route's roads, from which the app picks what to show.
 *
 * @property curvyDistance How much of the route is on curvy roads.
 * @property ascent How much the route climbs.
 */
data class RouteStats(val curvyDistance: Length, val ascent: Length)

/**
 * What the phone's maps app gets to follow the route.
 *
 * @property waypoints In driving order; the first is the route's start.
 * @property followedShare Share of the route's distance the maps app is expected to follow, from 0 to 1.
 */
data class HandOff(val waypoints: List<GeoPoint>, val followedShare: Double)

/**
 * A route of a [TripPlan].
 *
 * @property id Saving the route or recording a drive on it refers to it by this ID.
 * @property extraDuration How much longer it takes than the fastest route of its plan; zero for the fastest route.
 * @property extraDistance How much longer it is than the fastest route of its plan; zero for the fastest route.
 * @property funLevel How fun the route is, from 1 to 5, on one scale for every route, the fastest included.
 * @property line The route's line, from the start to the destination.
 * @property attribution Attribution for the route data, shown with the route.
 */
data class Route(
    val id: String,
    val kind: RouteKind,
    val start: Place,
    val destination: Place,
    val duration: Duration,
    val distance: Length,
    val extraDuration: Duration,
    val extraDistance: Length,
    val funLevel: Int,
    val stats: RouteStats,
    val line: List<GeoPoint>,
    val handOff: HandOff,
    val attribution: String,
)
