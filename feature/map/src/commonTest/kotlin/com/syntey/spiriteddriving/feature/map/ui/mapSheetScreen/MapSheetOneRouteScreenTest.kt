package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MapSheetOneRouteScreenTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `a saved route loads and shows its route`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()

        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(OneRouteStatus.Ready, state.oneRouteStep?.status)
        assertNotNull(state.oneRouteRow)
        assertEquals(FakeSavedRoutesSource.Recorded.name, state.oneRouteTitle)
        assertEquals(SheetHeight.Half, state.sheetHeight)
    }

    @Test
    fun `a past drive loads and shows its route`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(pastDrives = listOf(FakeTripHistorySource.Recorded))
        advanceUntilIdle()

        trip.screen.showPastDrive(FakeTripHistorySource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertIs<OneRouteOrigin.Past>(state.oneRouteStep?.origin)
        assertEquals(OneRouteStatus.Ready, state.oneRouteStep?.status)
        assertNotNull(state.oneRouteRow)
        assertEquals(FakeTripHistorySource.Recorded.destination.name, state.oneRouteTitle)
    }

    @Test
    fun `a saved route offline shows offline and Retry loads it once there is a connection`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        trip.savedRoutesSource.error = OfflineException(cause = null)

        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()
        assertEquals(OneRouteStatus.Offline, trip.screen.viewState.value.oneRouteStep?.status)

        trip.savedRoutesSource.error = null
        trip.screen.actions.onOneRouteRetry()
        advanceUntilIdle()
        assertEquals(OneRouteStatus.Ready, trip.screen.viewState.value.oneRouteStep?.status)
    }

    @Test
    fun `a saved route that fails shows failed and Retry loads it`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        trip.savedRoutesSource.error = IllegalStateException("Backend down")

        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()
        assertEquals(OneRouteStatus.Failed, trip.screen.viewState.value.oneRouteStep?.status)

        trip.savedRoutesSource.error = null
        trip.screen.actions.onOneRouteRetry()
        advanceUntilIdle()
        assertEquals(OneRouteStatus.Ready, trip.screen.viewState.value.oneRouteStep?.status)
    }

    @Test
    fun `closing the one-route step collapses the sheet`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        trip.screen.actions.onOneRouteClose()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.oneRouteStep)
        assertEquals(SheetHeight.Collapsed, state.sheetHeight)
    }

    @Test
    fun `saving a saved route's route again points the step at the new saved route`() = runTest(main.dispatcher) {
        val original = FakeSavedRoutesSource.Recorded.copy(id = SavedRouteId("saved-9"))
        val trip = mapSheetFixture(signedIn = true, savedRoutes = listOf(original))
        advanceUntilIdle()
        trip.screen.showSavedRoute(original.id)
        advanceUntilIdle()
        // Saved, so Save removes it first; saving it again then makes a new saved route
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.savedRouteOnScreen)

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        val saved = assertNotNull(state.savedRoutes.singleOrNull())
        assertNotEquals(original.id, saved.id)
        assertEquals(saved.id.value, state.oneRouteStep?.origin?.id)
        assertEquals(saved, state.savedRouteOnScreen)
    }

    @Test
    fun `planning new routes on a past drive plans a trip to its destination`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise, pastDrives = listOf(FakeTripHistorySource.Recorded))
        advanceUntilIdle()
        sendFix(trip, Points.prague)

        trip.screen.planNewRoutes(FakeTripHistorySource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.oneRouteStep)
        assertEquals(FakeTripHistorySource.Recorded.destination.location, state.planning?.destination?.location)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
    }

    @Test
    fun `touching and holding the map replaces a saved route with a new trip`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.oneRouteStep)
        assertEquals(Points.pinned, state.planning?.destination?.location)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
    }
}
