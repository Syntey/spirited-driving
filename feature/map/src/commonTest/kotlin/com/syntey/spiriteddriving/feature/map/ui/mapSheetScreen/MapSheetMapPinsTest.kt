package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

class MapSheetMapPinsTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `a point touched on the map pins the destination`() = runTest(main.dispatcher) {
        val trip = planned()

        assertEquals(Points.pinned, trip.screen.viewState.value.map.destination)
        assertNull(trip.screen.viewState.value.map.start)
    }

    @Test
    fun `a point picked for the start pins the start and keeps the destination`() = runTest(main.dispatcher) {
        val trip = planned()
        trip.planning.actions.onStartClick()
        advanceUntilIdle()

        trip.screen.actions.onMapLongPress(Points.prague)
        advanceUntilIdle()

        val map = trip.screen.viewState.value.map
        assertEquals(Points.prague, map.start)
        assertEquals(Points.pinned, map.destination)
    }

    @Test
    fun `closing route planning clears the start and destination pins`() = runTest(main.dispatcher) {
        val trip = planned()
        trip.planning.actions.onStartClick()
        advanceUntilIdle()
        trip.screen.actions.onMapLongPress(Points.prague)
        advanceUntilIdle()

        trip.planning.actions.onCloseClick()
        advanceUntilIdle()

        val map = trip.screen.viewState.value.map
        assertNull(map.start)
        assertNull(map.destination)
    }

    @Test
    fun `the rating step pins the drive's start and destination`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        val question = assertNotNull(trip.screen.viewState.value.ratingStep).question
        val map = trip.screen.viewState.value.map
        assertEquals(question.start, map.start)
        assertEquals(question.destination.location, map.destination)
    }

    @Test
    fun `closing the rating step clears its pins`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        trip.rating.actions.onCloseClick()
        advanceUntilIdle()

        val map = trip.screen.viewState.value.map
        assertNull(map.start)
        assertNull(map.destination)
    }

    @Test
    fun `a saved route pins its start and destination`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()

        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        val route = FakeSavedRoutesSource.Recorded.route
        val map = trip.screen.viewState.value.map
        assertEquals(route.start.location, map.start)
        assertEquals(route.destination.location, map.destination)
    }

    @Test
    fun `closing a saved route clears its pins`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        trip.oneRoute.actions.onCloseClick()
        advanceUntilIdle()

        val map = trip.screen.viewState.value.map
        assertNull(map.start)
        assertNull(map.destination)
    }

    private fun TestScope.planned(): MapSheetFixture {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()
        return trip
    }
}
