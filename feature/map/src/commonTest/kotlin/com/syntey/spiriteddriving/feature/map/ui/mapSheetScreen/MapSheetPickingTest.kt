package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapSheetPickingTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `touching and holding the map picks a destination and plans a trip to it`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(Points.pinned, trip.planningState?.planning?.destination?.location)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
        assertTrue(trip.planning.viewState.value.routeRows.isNotEmpty())
    }

    @Test
    fun `a picked destination gets its name once it is named`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)

        trip.screen.actions.onMapLongPress(Points.pinned)
        // Shown at once with the point's coordinates as its area, and no name until the backend names the point
        assertNull(trip.planningState?.planning?.destination?.name)

        advanceUntilIdle()
        assertEquals(FakePlaceSearch.RecordedPlaceAt.name, trip.planningState?.planning?.destination?.name)
    }

    @Test
    fun `a picked point is kept among recent places with its name`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()

        val named = FakePlaceSearch.RecordedPlaceAt.copy(location = Points.pinned)
        assertEquals(listOf(named), trip.screen.viewState.value.recentRows.map { it.place })
    }

    @Test
    fun `touching and holding outside the covered area shows not covered`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture()
        advanceUntilIdle()

        trip.screen.actions.onMapLongPress(Points.outside)
        advanceUntilIdle()

        assertEquals(listOf<MapSheetUiEvent>(MapSheetUiEvent.NotCovered), trip.events)
    }

    @Test
    fun `touching and holding outside the covered area doesn't plan`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture()
        advanceUntilIdle()

        trip.screen.actions.onMapLongPress(Points.outside)
        advanceUntilIdle()

        assertNull(trip.planningState)
    }

    @Test
    fun `touching and holding the map while the start search is open picks the start and plans from it`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture()
        advanceUntilIdle()
        val destination = searchForCovered(trip)
        trip.screen.actions.onSuggestionClick(destination)
        advanceUntilIdle()
        assertNotNull(trip.screen.viewState.value.startSearch)

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertEquals(Points.pinned, trip.planningState?.planning?.start?.location)
        assertEquals(destination.place, trip.planningState?.planning?.destination)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }
}
