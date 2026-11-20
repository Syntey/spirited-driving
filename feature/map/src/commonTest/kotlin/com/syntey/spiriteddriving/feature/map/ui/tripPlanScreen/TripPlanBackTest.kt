package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class TripPlanBackTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `back closes the rating step first`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.ratingStep)
        assertEquals(SheetHeight.Collapsed, state.sheetHeight)
    }

    @Test
    fun `back closes the start search before route planning`() = runTest(main.dispatcher) {
        val trip = planned()
        trip.screen.actions.onStartClick()
        advanceUntilIdle()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.startSearch)
        assertNotNull(trip.screen.viewState.value.planning)

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.planning)
    }

    @Test
    fun `back closes route planning before the pulled-up search`() = runTest(main.dispatcher) {
        val trip = planned()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()
        val state = trip.screen.viewState.value
        assertNull(state.planning)
        assertEquals(SheetHeight.Full, state.sheetHeight)
    }

    @Test
    fun `back closes the pulled-up search last`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.screen.actions.onSearchFocus()
        advanceUntilIdle()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()
        assertEquals(SheetHeight.Collapsed, trip.screen.viewState.value.sheetHeight)
        assertFalse(trip.lifecycle.back())
    }

    @Test
    fun `back on the collapsed map isn't handled`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        assertFalse(trip.lifecycle.back())
    }

    @Test
    fun `back closes a saved route shown over the pulled-up search`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        trip.screen.actions.onSearchFocus()
        advanceUntilIdle()
        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.oneRouteStep)
        assertEquals(SheetHeight.Collapsed, state.sheetHeight)
    }

    @Test
    fun `back closes a past drive's route shown over the pulled-up search`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(pastDrives = listOf(FakeTripHistorySource.Recorded))
        advanceUntilIdle()
        trip.screen.actions.onSearchFocus()
        advanceUntilIdle()
        trip.screen.showPastDrive(FakeTripHistorySource.Recorded.id)
        advanceUntilIdle()

        assertTrue(trip.lifecycle.back())
        advanceUntilIdle()

        assertNull(trip.screen.viewState.value.oneRouteStep)
    }

    private fun TestScope.planned(): TripPlanFixture {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()
        return trip
    }
}
