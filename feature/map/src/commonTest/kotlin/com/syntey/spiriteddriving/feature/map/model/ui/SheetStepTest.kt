package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SheetStepTest {
    @Test
    fun `a rating step with an empty drive line has no route line`() {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val step = RatingStep(
            question = DriveQuestion(
                driveId = "drive",
                startedAt = Instant.parse("2026-11-16T13:20:00Z"),
                routeNumber = 1,
                start = route.start.location,
                destination = route.destination,
                duration = route.duration,
                distance = route.distance,
                line = emptyList(),
                attribution = route.attribution,
            ),
            driveTime = "Today, 14:20".desc(),
        )

        assertNull(step.routeLines.singleOrNull())
    }

    @Test
    fun `a one-route step that hasn't loaded frames its origin's start and destination`() {
        val savedRoute = FakeSavedRoutesSource.Recorded
        val step = OneRouteStep(origin = OneRouteOrigin.Saved(savedRoute), route = null)

        assertTrue(step.routeBounds.holds(savedRoute.route.start.location))
        assertTrue(step.routeBounds.holds(savedRoute.route.destination.location))
    }

    private fun GeoBounds.holds(point: GeoPoint): Boolean =
        point.latitude in southWest.latitude..northEast.latitude && point.longitude in southWest.longitude..northEast.longitude
}
