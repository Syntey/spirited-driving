package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.data.network.dto.drive.DrivePageDto
import com.syntey.spiriteddriving.data.network.dto.drive.DriveSummaryDto
import com.syntey.spiriteddriving.data.network.dto.drive.RatingDto
import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlaceDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteStatsDto
import com.syntey.spiriteddriving.data.network.dto.plan.RouteSummaryDto
import com.syntey.spiriteddriving.data.network.injection.NetworkModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TripHistoryMappingTest {

    @Test
    fun `every field the app uses maps to the past drive`() {
        val drive = summaryDto(rating = RatingDto(thumb = "UP", reasons = listOf("CURVY_ROADS"), ratedAt = RATED_AT)).toDomain()

        assertEquals(
            PastDrive(
                id = DRIVE_ID,
                startedAt = Instant.parse("2026-11-08T09:30:00Z"),
                routeId = ROUTE_ID,
                kind = RouteKind.Fun,
                start = Place(name = "Vinohradská 12", area = "Prague 2", location = prague),
                destination = Place(name = "Karlštejn Castle", area = "Karlštejn, Central Bohemia", location = karlstejn),
                duration = 3900.seconds,
                distance = 58.kilometres,
                funLevel = 4,
                rating = Rating(Thumb.Up, setOf(RatingReason.CurvyRoads)),
            ),
            drive,
        )
    }

    @Test
    fun `a drive without a rating is not rated`() {
        assertNull(summaryDto(rating = null).toDomain().rating)
    }

    @Test
    fun `a down rating keeps its thumb and reasons`() {
        val rating = summaryDto(rating = RatingDto(thumb = "DOWN", reasons = listOf("TOO_NARROW", "ROADWORKS"), ratedAt = RATED_AT))
            .toDomain()
            .rating

        assertEquals(Rating(Thumb.Down, setOf(RatingReason.TooNarrow, RatingReason.Roadworks)), rating)
    }

    @Test
    fun `an unknown thumb leaves the drive unrated`() {
        assertNull(ratingDto(thumb = "MAYBE").toDomain())
    }

    @Test
    fun `an unknown reason is dropped`() {
        val rating = ratingDto(thumb = "UP", reasons = listOf("NICE_VIEWS", "SOMETHING_NEW")).toDomain()

        assertEquals(Rating(Thumb.Up, setOf(RatingReason.NiceViews)), rating)
    }

    @Test
    fun `a reason of the other thumb is dropped`() {
        val rating = ratingDto(thumb = "UP", reasons = listOf("CURVY_ROADS", "BORING_ROADS")).toDomain()

        assertEquals(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads)), rating)
    }

    @Test
    fun `FASTEST maps to the fastest route`() {
        assertEquals(RouteKind.Fastest, summaryDto(kind = "FASTEST").toDomain().kind)
    }

    @Test
    fun `any other kind maps to a fun route`() {
        assertEquals(RouteKind.Fun, summaryDto(kind = "FUN").toDomain().kind)
    }

    @Test
    fun `a page with a next cursor decodes`() {
        val page = NetworkModule().restApiJson().decodeFromString<DrivePageDto>(RECORDED_PAGE)

        assertEquals("opaque-cursor", page.nextCursor)
        assertEquals(listOf(DRIVE_ID, "5d1e0c4b-2a7f-4b8e-9c3d-7e6f5a4b3c2d"), page.items.map { it.id })
        assertEquals(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads)), page.items.first().toDomain().rating)
        assertNull(page.items.last().toDomain().rating)
    }

    @Test
    fun `a last page without a next cursor decodes to no cursor`() {
        val page = NetworkModule().restApiJson().decodeFromString<DrivePageDto>("""{"items":[]}""")

        assertNull(page.nextCursor)
        assertEquals(emptyList(), page.items)
    }

    private val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)

    private val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)

    private fun ratingDto(thumb: String, reasons: List<String> = emptyList()) =
        RatingDto(thumb = thumb, reasons = reasons, ratedAt = RATED_AT)

    private fun summaryDto(kind: String = "FUN", rating: RatingDto? = null) = DriveSummaryDto(
        id = DRIVE_ID,
        startedAt = "2026-11-08T09:30:00Z",
        route = RouteSummaryDto(
            id = ROUTE_ID,
            kind = kind,
            start = PlaceDto(location = GeoPointDto(lat = 50.0755, lon = 14.4378), name = "Vinohradská 12", area = "Prague 2"),
            destination = PlaceDto(
                location = GeoPointDto(lat = 49.9394, lon = 14.1882),
                name = "Karlštejn Castle",
                area = "Karlštejn, Central Bohemia",
            ),
            durationSeconds = 3900,
            distanceMeters = 58000,
            funLevel = 4,
            stats = RouteStatsDto(curvyDistanceMeters = 21000, ascentMeters = 640),
        ),
        rating = rating,
    )

    private companion object {
        const val DRIVE_ID = "2b7d4e1a-8c3f-4a9e-b5d6-0f1e2d3c4b5a"
        const val ROUTE_ID = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d"
        const val RATED_AT = "2026-11-08T10:05:00Z"

        // Shaped like the DrivePage schema, with a rated drive and an unrated one, and a next cursor
        val RECORDED_PAGE = """
            {
              "items": [
                {
                  "id": "$DRIVE_ID",
                  "startedAt": "2026-11-08T09:30:00Z",
                  "route": {
                    "id": "$ROUTE_ID",
                    "kind": "FUN",
                    "start": {"location": {"lat": 50.0755, "lon": 14.4378}, "name": "Vinohradská 12", "area": "Prague 2"},
                    "destination": {"location": {"lat": 49.9394, "lon": 14.1882}, "name": "Karlštejn Castle", "area": "Karlštejn, Central Bohemia"},
                    "durationSeconds": 3900,
                    "distanceMeters": 58000,
                    "extraDurationSeconds": 1620,
                    "extraDistanceMeters": 25000,
                    "funLevel": 4,
                    "stats": {"curvyDistanceMeters": 21000, "ascentMeters": 640},
                    "vignetteCountries": ["CZ"]
                  },
                  "rating": {"thumb": "UP", "reasons": ["CURVY_ROADS"], "ratedAt": "$RATED_AT"}
                },
                {
                  "id": "5d1e0c4b-2a7f-4b8e-9c3d-7e6f5a4b3c2d",
                  "startedAt": "2026-11-07T18:00:00Z",
                  "route": {
                    "id": "$ROUTE_ID",
                    "kind": "FASTEST",
                    "start": {"location": {"lat": 50.0755, "lon": 14.4378}, "name": "Vinohradská 12", "area": "Prague 2"},
                    "destination": {"location": {"lat": 49.9394, "lon": 14.1882}, "name": "Karlštejn Castle", "area": "Karlštejn, Central Bohemia"},
                    "durationSeconds": 2280,
                    "distanceMeters": 33000,
                    "extraDurationSeconds": 0,
                    "extraDistanceMeters": 0,
                    "funLevel": 1,
                    "stats": {"curvyDistanceMeters": 2000, "ascentMeters": 180},
                    "vignetteCountries": []
                  }
                }
              ],
              "nextCursor": "opaque-cursor"
            }
        """.trimIndent()
    }
}
