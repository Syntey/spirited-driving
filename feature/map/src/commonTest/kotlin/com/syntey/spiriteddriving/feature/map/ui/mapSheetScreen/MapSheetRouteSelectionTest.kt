package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MapSheetRouteSelectionTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `tapping a listed route selects it`() = runTest(main.dispatcher) {
        val trip = plannedFromPrague()
        assertEquals(FakeRoutePlanner.Recorded.funRoutes.first().id, trip.screen.viewState.value.selectedRouteRow?.id)

        trip.screen.actions.onRouteClick(FakeRoutePlanner.Recorded.fastest.id)

        assertEquals(FakeRoutePlanner.Recorded.fastest.id, trip.screen.viewState.value.selectedRouteRow?.id)
    }

    @Test
    fun `tapping a route that isn't listed changes nothing`() = runTest(main.dispatcher) {
        val trip = plannedFromPrague()
        val before = trip.screen.viewState.value

        trip.screen.actions.onRouteClick("not-a-listed-route")

        assertEquals(before, trip.screen.viewState.value)
    }

    private fun TestScope.plannedFromPrague(): MapSheetFixture {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        return trip
    }
}
