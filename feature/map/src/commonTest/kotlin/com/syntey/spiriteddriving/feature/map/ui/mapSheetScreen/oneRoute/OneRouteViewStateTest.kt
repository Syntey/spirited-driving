package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OneRouteViewStateTest {

    @Test
    fun `a saved route's row is route 1 without an extra`() {
        val row = assertNotNull(savedRouteState().row)

        assertEquals(1, row.number)
        assertNull(row.extra)
        assertTrue(row.isSelected)
    }

    @Test
    fun `a past drive's header is its destination's name`() {
        assertEquals("Karlštejn Castle", pastDriveState(pastDrive).title)
    }

    @Test
    fun `a past drive without a destination name is headed by its area`() {
        val unnamed = pastDrive.copy(destination = Place(name = null, area = "49.9, 14.2", location = pastRoute.destination.location))

        assertEquals("49.9, 14.2", pastDriveState(unnamed).title)
    }

    private val savedRoute = FakeSavedRoutesSource.Recorded

    private val savedRouteRoute = FakeRoutePlanner.Recorded.funRoutes.first()

    private fun savedRouteState(route: Route? = savedRouteRoute) =
        OneRouteViewState(step = OneRouteStep(origin = OneRouteOrigin.Saved(savedRoute), route = route))

    private val pastRoute = FakeRoutePlanner.Recorded.funRoutes.first()

    private val pastDrive = PastDrive(
        id = "drive-1",
        startedAt = Instant.parse("2026-11-05T13:20:00Z"),
        routeId = pastRoute.id,
        kind = RouteKind.Fun,
        start = Place(name = null, area = "Smíchov, Prague", location = GeoPoint(latitude = 50.07, longitude = 14.40)),
        destination = Place(name = "Karlštejn Castle", area = "Beroun", location = pastRoute.destination.location),
        duration = pastRoute.duration,
        distance = pastRoute.distance,
        funLevel = pastRoute.funLevel,
    )

    private fun pastDriveState(drive: PastDrive) = OneRouteViewState(step = OneRouteStep(origin = OneRouteOrigin.Past(drive)))
}
