package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
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

class TripPlanSheetHeightTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `the rating step opens the sheet to half`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        assertNotNull(trip.screen.viewState.value.ratingStep)
        assertEquals(SheetHeight.Half, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `closing the rating step collapses the sheet`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        trip.screen.actions.onRatingClose()
        advanceUntilIdle()

        assertEquals(SheetHeight.Collapsed, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `planning a trip opens the sheet to half`() = runTest(main.dispatcher) {
        val trip = planned()

        assertEquals(SheetHeight.Half, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `closing route planning opens the pulled-up search`() = runTest(main.dispatcher) {
        val trip = planned()

        trip.screen.actions.onRoutePlanningClose()
        advanceUntilIdle()

        assertEquals(SheetHeight.Full, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `opening the start search opens the sheet to full`() = runTest(main.dispatcher) {
        val trip = planned()

        trip.screen.actions.onStartClick()
        advanceUntilIdle()

        assertEquals(SheetHeight.Full, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `closing the start search returns the sheet to half`() = runTest(main.dispatcher) {
        val trip = planned()
        trip.screen.actions.onStartClick()
        advanceUntilIdle()

        trip.screen.actions.onSearchClose()
        advanceUntilIdle()

        assertNull(trip.screen.viewState.value.startSearch)
        assertEquals(SheetHeight.Half, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `dragging the start search down closes it and keeps the planning`() = runTest(main.dispatcher) {
        val trip = planned()
        trip.screen.actions.onStartClick()
        advanceUntilIdle()

        trip.screen.actions.onSheetHeightChange(SheetHeight.Collapsed)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertNotNull(state.planning)
        assertEquals(SheetHeight.Collapsed, state.sheetHeight)
    }

    @Test
    fun `focusing the search opens the sheet to full`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onSearchFocus()
        advanceUntilIdle()

        assertEquals(SheetHeight.Full, trip.screen.viewState.value.sheetHeight)
    }

    @Test
    fun `closing the search collapses the sheet`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.screen.actions.onSearchFocus()
        advanceUntilIdle()

        trip.screen.actions.onSearchClose()
        advanceUntilIdle()

        assertEquals(SheetHeight.Collapsed, trip.screen.viewState.value.sheetHeight)
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
