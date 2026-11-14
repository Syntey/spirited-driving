package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.HandOff
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class HandOffLinksTest {

    private val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)
    private val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)
    private val middle = GeoPoint(latitude = 50.001, longitude = 14.215)
    private val lastWaypoint = GeoPoint(latitude = 49.968, longitude = 14.12)

    @Test
    fun `Google Maps link carries the destination and the waypoints in driving order`() {
        val route = route(waypoints = listOf(prague, middle, lastWaypoint), destination = karlstejn)

        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=49.9394,14.1882" +
                "&waypoints=50.0755,14.4378%7C50.001,14.215%7C49.968,14.12" +
                "&travelmode=driving&dir_action=navigate",
            googleMapsLink(route),
        )
    }

    @Test
    fun `Apple Maps link carries the destination and one waypoint per point in driving order`() {
        val route = route(waypoints = listOf(prague, middle, lastWaypoint), destination = karlstejn)

        assertEquals(
            "https://maps.apple.com/directions?destination=49.9394,14.1882" +
                "&waypoint=50.0755,14.4378&waypoint=50.001,14.215&waypoint=49.968,14.12&mode=driving",
            appleMapsLink(route),
        )
    }

    @Test
    fun `Google Maps link has no waypoints parameter without waypoints`() {
        val route = route(waypoints = emptyList(), destination = karlstejn)

        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=49.9394,14.1882&travelmode=driving&dir_action=navigate",
            googleMapsLink(route),
        )
    }

    @Test
    fun `Apple Maps link has no waypoint parameter without waypoints`() {
        val route = route(waypoints = emptyList(), destination = karlstejn)

        assertEquals("https://maps.apple.com/directions?destination=49.9394,14.1882&mode=driving", appleMapsLink(route))
    }

    @Test
    fun `small and negative coordinates are written without scientific notation`() {
        val route = route(
            waypoints = listOf(GeoPoint(latitude = 49.5, longitude = 16.0)),
            destination = GeoPoint(latitude = -0.000001, longitude = 0.00001),
        )

        assertEquals(
            "https://maps.apple.com/directions?destination=-0.000001,0.00001&waypoint=49.5,16&mode=driving",
            appleMapsLink(route),
        )
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=-0.000001,0.00001&waypoints=49.5,16" +
                "&travelmode=driving&dir_action=navigate",
            googleMapsLink(route),
        )
    }

    @Test
    fun `coordinates beyond six decimals are rounded to six`() {
        val route = route(
            waypoints = listOf(GeoPoint(latitude = 49.1234567, longitude = 14.99999999)),
            destination = karlstejn,
        )

        assertEquals(
            "https://maps.apple.com/directions?destination=49.9394,14.1882&waypoint=49.123457,15&mode=driving",
            appleMapsLink(route),
        )
    }

    private fun route(waypoints: List<GeoPoint>, destination: GeoPoint) = Route(
        id = "3f2b8c1e-5d4a-4e7b-9a6c-1d2e3f4a5b6c",
        kind = RouteKind.Fun,
        start = Place(name = null, area = null, location = prague),
        destination = Place(name = null, area = null, location = destination),
        duration = 3900.seconds,
        distance = 58000.metres,
        extraDuration = 0.seconds,
        extraDistance = 0.metres,
        funLevel = 4,
        stats = RouteStats(curvyDistance = 21000.metres, ascent = 640.metres),
        line = emptyList(),
        handOff = HandOff(waypoints = waypoints, followedShare = 1.0),
        attribution = "© OpenStreetMap contributors",
    )
}
