package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TripPlanStartSearchTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `Start opens the start search over planning`() = runTest(main.dispatcher) {
        val trip = planFromPrague()

        trip.screen.actions.onStartClick()

        val state = trip.screen.viewState.value
        assertNotNull(state.startSearch)
        assertNotNull(state.planning)
    }

    @Test
    fun `closing the start search keeps the planning`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.screen.actions.onStartClick()

        trip.screen.actions.onSearchClose()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
        assertEquals(SheetHeight.Half, state.sheetHeight)
    }

    @Test
    fun `choosing a place in the start search plans from it`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        val destination = searchForCovered(trip, text = "karlst")
        trip.screen.actions.onSuggestionClick(destination)
        advanceUntilIdle()
        trip.screen.actions.onStartClick()
        trip.screen.actions.onSearchTextChange("karl")
        advanceUntilIdle()
        // A different covered place than the destination, last in the list so it isn't the first one
        val start = trip.screen.viewState.value.suggestionRows.last { it.suggestion.isCovered }.suggestion

        trip.screen.actions.onSuggestionClick(start)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertEquals(start.place, state.planning?.start)
        assertEquals(destination.place, state.planning?.destination)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
    }

    @Test
    fun `choosing a place in the start search brings the destination search back as it was`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        val destination = searchForCovered(trip, text = "karlst")
        trip.screen.actions.onSuggestionClick(destination)
        advanceUntilIdle()
        val destinationSearch = trip.screen.viewState.value.search
        trip.screen.actions.onStartClick()
        trip.screen.actions.onSearchTextChange("karl")
        advanceUntilIdle()
        val start = trip.screen.viewState.value.suggestionRows.last { it.suggestion.isCovered }.suggestion

        trip.screen.actions.onSuggestionClick(start)
        advanceUntilIdle()

        // Recent places are the current ones, as the start was kept among them
        val search = trip.screen.viewState.value.search
        assertEquals(destinationSearch.text, search.text)
        assertEquals(destinationSearch.status, search.status)
        assertEquals(destinationSearch.suggestions, search.suggestions)
    }

    @Test
    fun `My location as the start plans from the user's location`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.screen.actions.onStartClick()
        assertEquals(MyLocationRow.Available, trip.screen.viewState.value.myLocationRow)

        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertNull(state.planning?.start)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
        assertEquals(SheetHeight.Half, state.sheetHeight)
    }

    @Test
    fun `My location needing precise location asks for it`() = runTest(main.dispatcher) {
        val trip = approximateTripAwaitingStart()
        trip.locationPermission.access = LocationAccess.Precise

        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        assertEquals(LocationAccess.Precise, trip.screen.viewState.value.locationAccess)
    }

    @Test
    fun `My location needing precise location uses it once usable`() = runTest(main.dispatcher) {
        val trip = approximateTripAwaitingStart()
        trip.locationPermission.access = LocationAccess.Precise
        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        // Precise now, but no fix yet: the trip waits for the location, with no start picked by hand
        val waiting = trip.screen.viewState.value
        assertNull(waiting.startSearch)
        assertNull(waiting.planning?.start)
        assertEquals(PlanningStatus.Planning, waiting.planning?.status)

        sendFix(trip, Points.prague)

        assertEquals(PlanningStatus.Planned, trip.screen.viewState.value.planning?.status)
    }

    @Test
    fun `My location needing access asks for access`() = runTest(main.dispatcher) {
        val trip = deniedTripAwaitingStart()
        trip.locationPermission.access = LocationAccess.Precise

        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        assertEquals(LocationAccess.Precise, trip.screen.viewState.value.locationAccess)
    }

    @Test
    fun `My location needing access uses it once usable`() = runTest(main.dispatcher) {
        val trip = deniedTripAwaitingStart()
        trip.locationPermission.access = LocationAccess.Precise
        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        val waiting = trip.screen.viewState.value
        assertNull(waiting.startSearch)
        assertEquals(PlanningStatus.Planning, waiting.planning?.status)

        sendFix(trip, Points.prague)

        assertEquals(PlanningStatus.Planned, trip.screen.viewState.value.planning?.status)
    }

    @Test
    fun `My location outside the covered area shows not covered`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.outside)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        trip.screen.actions.onStartClick()
        assertEquals(MyLocationRow.OutsideCoveredArea, trip.screen.viewState.value.myLocationRow)

        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.NotCovered), trip.events)
        assertNotNull(trip.screen.viewState.value.startSearch)
    }

    @Test
    fun `dragging the start search down closes it`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.screen.actions.onStartClick()

        trip.screen.actions.onSheetHeightChange(SheetHeight.Collapsed)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        // The drag's own height wins over the step's, so the sheet ends collapsed where the user let go
        assertEquals(SheetHeight.Collapsed, state.sheetHeight)
        assertNotNull(state.planning)
    }

    /** A trip planned from the user's location at Prague, with the destination's first covered suggestion chosen. */
    private fun kotlinx.coroutines.test.TestScope.planFromPrague(): TripPlanFixture {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        return trip
    }

    /** Approximate access: a destination chosen opens the start search, where My location needs precise location. */
    private fun kotlinx.coroutines.test.TestScope.approximateTripAwaitingStart(): TripPlanFixture {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Approximate)
        advanceUntilIdle()
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        assertEquals(MyLocationRow.NeedsPreciseLocation, trip.screen.viewState.value.myLocationRow)
        return trip
    }

    /** Denied access: a destination chosen opens the start search, where My location needs access. */
    private fun kotlinx.coroutines.test.TestScope.deniedTripAwaitingStart(): TripPlanFixture {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Denied)
        advanceUntilIdle()
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        assertEquals(MyLocationRow.NeedsLocationAccess, trip.screen.viewState.value.myLocationRow)
        return trip
    }
}
