package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.decodePolyline
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * [RoutePlanner] that answers every request with [plan]. Records the requests it got.
 */
class FakeRoutePlanner(private val plan: TripPlan = Recorded) : RoutePlanner {

    val requests: List<TripRequest>
        field = mutableListOf<TripRequest>()

    override suspend fun plan(request: TripRequest): TripPlan {
        requests += request
        return plan
    }

    companion object {
        /** The API spec's example plan from Prague to Karlštejn Castle: the fastest route and one fun route. */
        val Recorded: TripPlan = run {
            val start = Place(
                name = "Vinohradská 12",
                area = "Prague 2",
                location = GeoPoint(latitude = 50.0755, longitude = 14.4378),
            )
            val destination = Place(
                name = "Karlštejn Castle",
                area = "Karlštejn, Central Bohemia",
                location = GeoPoint(latitude = 49.9394, longitude = 14.1882),
            )
            TripPlan(
                expiresAt = Instant.parse("2026-11-07T18:20:00Z"),
                fastest = Route(
                    id = "3f2b8c1e-5d4a-4e7b-9a6c-1d2e3f4a5b6c",
                    kind = RouteKind.Fastest,
                    start = start,
                    destination = destination,
                    duration = 2280.seconds,
                    distance = 33.kilometres,
                    extraDuration = 0.seconds,
                    extraDistance = 0.metres,
                    funLevel = 1,
                    stats = RouteStats(curvyDistance = 2.kilometres, ascent = 180.metres),
                    line = decodePolyline("{jspHg{bwAbuGnjGf_GvoJfmEnxO~lBfaK"),
                    handOff = HandOff(
                        waypoints = listOf(GeoPoint(latitude = 50.0755, longitude = 14.4378)),
                        followedShare = 1.0,
                    ),
                    attribution = "© OpenStreetMap contributors",
                ),
                funRoutes = listOf(
                    Route(
                        id = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
                        kind = RouteKind.Fun,
                        start = start,
                        destination = destination,
                        duration = 3900.seconds,
                        distance = 58.kilometres,
                        extraDuration = 1620.seconds,
                        extraDistance = 25.kilometres,
                        funLevel = 4,
                        stats = RouteStats(curvyDistance = 21.kilometres, ascent = 640.metres),
                        line = decodePolyline("{jspHg{bwAjpEfbRf_GflWfmEvpQnoBwyEfaAonE"),
                        handOff = HandOff(
                            waypoints = listOf(
                                GeoPoint(latitude = 50.0755, longitude = 14.4378),
                                GeoPoint(latitude = 50.0010, longitude = 14.2150),
                                GeoPoint(latitude = 49.9680, longitude = 14.1200),
                                GeoPoint(latitude = 49.9500, longitude = 14.1550),
                            ),
                            followedShare = 0.97,
                        ),
                        attribution = "© OpenStreetMap contributors",
                    ),
                ),
                closestFunRouteBudget = null,
            )
        }
    }
}
