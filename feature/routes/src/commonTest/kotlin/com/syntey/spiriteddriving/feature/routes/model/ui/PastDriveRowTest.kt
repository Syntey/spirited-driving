package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveDateSpokenText
import com.syntey.spiriteddriving.core.resources.datetime.driveDateText
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class PastDriveRowTest {

    @Test
    fun `a fun drive from the last week can be rated`() {
        val row = drive(startedAt = NOW - 6.days).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(PastDriveRowEnd.Rate, row.end)
    }

    @Test
    fun `a fun drive older than a week can't be rated any more`() {
        val row = drive(startedAt = NOW - 8.days).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(PastDriveRowEnd.None, row.end)
    }

    @Test
    fun `a drive on the fastest route can't be rated`() {
        val row = drive(kind = RouteKind.Fastest).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(PastDriveRowEnd.None, row.end)
    }

    @Test
    fun `a rating shows its thumb in place of the Rate button`() {
        val row = drive(rating = Rating(Thumb.Up)).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(PastDriveRowEnd.Rated(Thumb.Up), row.end)
    }

    @Test
    fun `a rated drive from the fastest route shows its rating`() {
        val row = drive(kind = RouteKind.Fastest, rating = Rating(Thumb.Down)).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(PastDriveRowEnd.Rated(Thumb.Down), row.end)
    }

    @Test
    fun `details mark the fastest route`() {
        val row = drive(kind = RouteKind.Fastest).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.trip_history_row_fastest,
                driveDateText(NOW - 2.hours, NOW, PRAGUE),
                "Smíchov",
                distanceText(58.kilometres, DistanceUnit.Kilometres, isApproximate = false),
            ),
            row.details,
        )
    }

    @Test
    fun `a start without an area leaves the start out of the details`() {
        val row = drive(start = Place(name = null, area = null, location = START_POINT))
            .toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.trip_history_row_no_start,
                driveDateText(NOW - 2.hours, NOW, PRAGUE),
                distanceText(58.kilometres, DistanceUnit.Kilometres, isApproximate = false),
            ),
            row.details,
        )
    }

    @Test
    fun `a destination without a name is named by its area`() {
        val row = drive(destination = Place(name = null, area = "Karlštejn", location = END_POINT))
            .toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals("Karlštejn", row.destination)
    }

    @Test
    fun `the description reads the destination the date the start and the distance`() {
        val row = drive().toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.trip_history_description,
                "Karlštejn Castle",
                MR.strings.drive_feedback_today_day.desc(),
                "Smíchov",
                distanceText(58.kilometres, DistanceUnit.Kilometres, isApproximate = false),
            ),
            row.description,
        )
    }

    @Test
    fun `a rated drive's description names the rating`() {
        val row = drive(rating = Rating(Thumb.Down)).toRow(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.trip_history_description_rated,
                StringDesc.ResourceFormatted(
                    MR.strings.trip_history_description,
                    "Karlštejn Castle",
                    driveDateSpokenText(NOW - 2.hours, NOW, PRAGUE),
                    "Smíchov",
                    distanceText(58.kilometres, DistanceUnit.Kilometres, isApproximate = false),
                ),
                MR.strings.trip_history_rated_not_good.desc(),
            ),
            row.description,
        )
    }

    private fun drive(
        startedAt: Instant = NOW - 2.hours,
        kind: RouteKind = RouteKind.Fun,
        start: Place = Place(name = "Vinohradská 12", area = "Smíchov", location = START_POINT),
        destination: Place = Place(name = "Karlštejn Castle", area = "Karlštejn", location = END_POINT),
        rating: Rating? = null,
    ): PastDrive = PastDrive(
        id = "drive-1",
        startedAt = startedAt,
        routeId = "route-1",
        kind = kind,
        start = start,
        destination = destination,
        duration = 1.hours,
        distance = 58.kilometres,
        funLevel = 4,
        rating = rating,
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-16T15:00:00Z")
        val PRAGUE: TimeZone = TimeZone.of("Europe/Prague")
        val START_POINT = GeoPoint(latitude = 50.07, longitude = 14.40)
        val END_POINT = GeoPoint(latitude = 49.94, longitude = 14.19)
    }
}
