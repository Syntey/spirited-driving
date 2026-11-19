package com.syntey.spiriteddriving.data.network.savedroute

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import com.syntey.spiriteddriving.core.domain.route.RouteSummary
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteStatsDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteSummaryDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteListDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteSummaryDto
import com.syntey.spiriteddriving.data.network.injection.NetworkModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SavedRouteMappingTest {

    private val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)

    private val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)

    @Test
    fun `every field the app uses maps to the saved route`() {
        val saved = summaryDto().toDomain()

        assertEquals(
            SavedRoute(
                id = SavedRouteId("2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a"),
                name = "Karlštejn Castle",
                savedAt = Instant.parse("2026-11-08T09:30:00Z"),
                route = RouteSummary(
                    id = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
                    start = Place(name = "Vinohradská 12", area = "Prague 2", location = prague),
                    destination = Place(name = "Karlštejn Castle", area = "Karlštejn, Central Bohemia", location = karlstejn),
                    duration = 3900.seconds,
                    distance = 58.kilometres,
                    funLevel = 4,
                    stats = RouteStats(curvyDistance = 21.kilometres, ascent = 640.metres),
                ),
            ),
            saved,
        )
    }

    @Test
    fun `a missing area maps to no area`() {
        val saved = summaryDto(destinationArea = null).toDomain()

        assertEquals(null, saved.route.destination.area)
    }

    @Test
    fun `a list response decodes with the fields the app ignores`() {
        val list = NetworkModule().restApiJson().decodeFromString<SavedRouteListDto>(RECORDED_LIST)

        val saved = list.items.single().toDomain()

        assertEquals("Karlštejn Castle", saved.name)
        assertEquals(3900.seconds, saved.route.duration)
        assertEquals(58.kilometres, saved.route.distance)
        assertEquals(4, saved.route.funLevel)
    }

    private fun summaryDto(destinationArea: String? = "Karlštejn, Central Bohemia") = SavedRouteSummaryDto(
        id = "2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a",
        name = "Karlštejn Castle",
        savedAt = "2026-11-08T09:30:00Z",
        route = RouteSummaryDto(
            id = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
            kind = "FUN",
            start = PlaceDto(location = GeoPointDto(lat = 50.0755, lon = 14.4378), name = "Vinohradská 12", area = "Prague 2"),
            destination = PlaceDto(
                location = GeoPointDto(lat = 49.9394, lon = 14.1882),
                name = "Karlštejn Castle",
                area = destinationArea,
            ),
            durationSeconds = 3900,
            distanceMeters = 58000,
            funLevel = 4,
            stats = RouteStatsDto(curvyDistanceMeters = 21000, ascentMeters = 640),
        ),
    )

    private companion object {
        // Recorded from the local backend, trimmed to one item
        val RECORDED_LIST = """
            {
              "items": [
                {
                  "id": "2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a",
                  "name": "Karlštejn Castle",
                  "savedAt": "2026-11-08T09:30:00Z",
                  "route": {
                    "id": "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
                    "kind": "FUN",
                    "start": {
                      "location": {"lat": 50.0755, "lon": 14.4378},
                      "name": "Vinohradská 12",
                      "area": "Prague 2"
                    },
                    "destination": {
                      "location": {"lat": 49.9394, "lon": 14.1882},
                      "name": "Karlštejn Castle",
                      "area": "Karlštejn, Central Bohemia"
                    },
                    "durationSeconds": 3900,
                    "distanceMeters": 58000,
                    "extraDurationSeconds": 1620,
                    "extraDistanceMeters": 25000,
                    "funLevel": 4,
                    "stats": {"curvyDistanceMeters": 21000, "ascentMeters": 640}
                  }
                }
              ]
            }
        """.trimIndent()
    }
}
