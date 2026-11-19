package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.savedAs
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Instant

class TripPlanOneRouteTest {

    private val route = FakeRoutePlanner.Recorded.funRoutes.first()
    private val drive = PastDrive(
        id = "drive-1",
        startedAt = Instant.parse("2026-11-05T13:20:00Z"),
        routeId = route.id,
        kind = RouteKind.Fun,
        start = Place(name = null, area = "Smíchov, Prague", location = GeoPoint(latitude = 50.07, longitude = 14.40)),
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        funLevel = route.funLevel,
    )

    @Test
    fun `the selected route is saved when a saved route has its route ID`() {
        val state = stateWith(selectedRouteId = route.id, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertTrue(state.isSelectedRouteSaved)
    }

    @Test
    fun `the selected route isn't saved when no saved route has its route ID`() {
        val other = FakeRoutePlanner.Recorded.fastest.id
        val state = stateWith(selectedRouteId = other, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertFalse(state.isSelectedRouteSaved)
    }

    @Test
    fun `without a selected route nothing is saved`() {
        val state = stateWith(selectedRouteId = null, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertFalse(state.isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is saved while its saved route is kept`() {
        val state = stepWith(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertTrue(state.isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is unsaved once its saved route is removed`() {
        assertFalse(stepWith(savedRoutes = emptyList()).isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is saved by its own saved route and not by another one of its route`() {
        val other = FakeSavedRoutesSource.Recorded.copy(id = SavedRouteId("saved-2"))

        assertFalse(stepWith(savedRoutes = listOf(other)).isSelectedRouteSaved)
    }

    @Test
    fun `a past drive's step is saved while a saved route has the drive's route ID`() {
        val saved = FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = route.id))

        assertSame(saved, pastStepWith(savedRoutes = listOf(saved)).savedRouteOnScreen)
    }

    @Test
    fun `a past drive's step is unsaved once no saved route has its route ID`() {
        assertFalse(
            pastStepWith(
                savedRoutes = listOf(FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = "other"))),
            ).isSelectedRouteSaved,
        )
    }

    @Test
    fun `a past drive's step is unsaved when only other routes are saved`() {
        val other = FakeSavedRoutesSource.Recorded.copy(
            id = SavedRouteId("saved-2"),
            route = FakeSavedRoutesSource.Recorded.route.copy(id = "other-route"),
        )

        assertNull(pastStepWith(savedRoutes = listOf(other)).savedRouteOnScreen)
    }

    @Test
    fun `a name dialog finds the one-route step's route by its ID`() {
        val state = pastStepWith(savedRoutes = emptyList())

        assertEquals(route, state.routeOnScreen(route.id))
    }

    @Test
    fun `a saved route's step is about the saved route its route was saved as`() {
        val old = FakeSavedRoutesSource.Recorded
        val new = old.copy(id = SavedRouteId("saved-new"))
        val step = OneRouteStep(origin = OneRouteOrigin.Saved(old), route = route)

        assertEquals(OneRouteOrigin.Saved(new), step.savedAs(route.id, new).origin)
    }

    @Test
    fun `a past drive's step stays about its drive when its route is saved`() {
        val step = OneRouteStep(origin = OneRouteOrigin.Past(drive), route = route)

        assertEquals(step, step.savedAs(route.id, FakeSavedRoutesSource.Recorded))
    }

    @Test
    fun `saving a route other than the step's leaves the step as it is`() {
        val step = OneRouteStep(origin = OneRouteOrigin.Saved(FakeSavedRoutesSource.Recorded), route = route)

        assertEquals(
            step,
            step.savedAs(FakeRoutePlanner.Recorded.fastest.id, FakeSavedRoutesSource.Recorded.copy(id = SavedRouteId("saved-new"))),
        )
    }

    private fun stepWith(savedRoutes: List<SavedRoute>): TripPlanViewState = TripPlanViewState(
        sheetStep = OneRouteStep(origin = OneRouteOrigin.Saved(FakeSavedRoutesSource.Recorded), route = route),
        savedRoutes = savedRoutes.toImmutableList(),
    )

    private fun pastStepWith(savedRoutes: List<SavedRoute>): TripPlanViewState = TripPlanViewState(
        sheetStep = OneRouteStep(origin = OneRouteOrigin.Past(drive), route = route),
        savedRoutes = savedRoutes.toImmutableList(),
    )

    private fun stateWith(selectedRouteId: String?, savedRoutes: List<SavedRoute>): TripPlanViewState {
        val planning = RoutePlanningState(
            destination = Place(name = "Karlštejn Castle", area = null, location = route.destination.location),
            budget = TripPlanViewState().budgetChoice.budget,
            selectedRouteId = selectedRouteId,
        )
        return TripPlanViewState(planning = planning, savedRoutes = savedRoutes.toImmutableList())
    }
}
