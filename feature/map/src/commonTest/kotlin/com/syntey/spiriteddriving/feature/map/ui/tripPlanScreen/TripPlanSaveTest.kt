package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.RecordingTripPlanNavigation.Call
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripPlanSaveTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `saving signed out opens sign-in to save the route`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf(Call.SignInToSaveRoute), trip.navigation.calls)
        assertFalse(trip.screen.viewState.value.isSelectedRouteSaved)
    }

    @Test
    fun `a route is saved once the sign-in sheet closes signed in`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        trip.auth.emitUser(TEST_USER)
        advanceUntilIdle()
        trip.screen.onSignInToSaveFinished(isSignedIn = true)
        advanceUntilIdle()

        assertTrue(trip.screen.viewState.value.isSelectedRouteSaved)
    }

    @Test
    fun `a route isn't saved when the sign-in sheet closes signed out`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        trip.screen.onSignInToSaveFinished(isSignedIn = false)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertFalse(state.isSelectedRouteSaved)
        assertTrue(state.savedRoutes.isEmpty())
    }

    @Test
    fun `a taken name opens the name dialog with its suggestion`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(takenRoute))

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        val dialog = assertNotNull(trip.screen.viewState.value.nameDialog)
        assertEquals(takenRoute.name, dialog.takenName)
        assertTrue(dialog.suggestion.isNotBlank())
        assertNotEquals(takenRoute.name, dialog.suggestion)
    }

    @Test
    fun `dismissing the name dialog saves nothing`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(takenRoute))
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        trip.screen.actions.onRouteNameDismiss()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.nameDialog)
        assertFalse(state.isSelectedRouteSaved)
        assertEquals(listOf(takenRoute.name), state.savedRoutes.map { it.name })
    }

    @Test
    fun `a name typed in the name dialog saves the route under it`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(takenRoute))
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        trip.screen.actions.onRouteNameSave("Karlštejn Lookout")
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.nameDialog)
        assertTrue(state.isSelectedRouteSaved)
        assertTrue("Karlštejn Lookout" in state.savedRoutes.map { it.name })
    }

    @Test
    fun `saving offline says so`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn()
        trip.savedRoutesSource.error = OfflineException(cause = null)

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.SaveRouteOffline), trip.events)
        assertFalse(trip.screen.viewState.value.isSelectedRouteSaved)
    }

    @Test
    fun `saving that fails says so`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn()
        trip.savedRoutesSource.error = IllegalStateException("Backend down")

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.SaveRouteFailed), trip.events)
        assertFalse(trip.screen.viewState.value.isSavingRoute)
    }

    @Test
    fun `removing a saved route unsaves it`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        assertTrue(trip.screen.viewState.value.isSelectedRouteSaved)

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertFalse(trip.screen.viewState.value.isSelectedRouteSaved)
    }

    @Test
    fun `removing offline keeps the route saved and says so`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        trip.savedRoutesSource.error = OfflineException(cause = null)

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.RemoveRouteOffline), trip.events)
        assertTrue(trip.screen.viewState.value.isSelectedRouteSaved)
    }

    @Test
    fun `removing that fails keeps the route saved and says so`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))
        trip.savedRoutesSource.error = IllegalStateException("Backend down")

        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.RemoveRouteFailed), trip.events)
        val state = trip.screen.viewState.value
        assertTrue(state.isSelectedRouteSaved)
        assertFalse(state.isSavingRoute)
    }

    @Test
    fun `saving is locked while a save runs`() = runTest(main.dispatcher) {
        val trip = planFromSignedIn()

        trip.screen.actions.onSaveClick()
        assertTrue(trip.screen.viewState.value.isSavingRoute)
        trip.screen.actions.onSaveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.nameDialog)
        assertEquals(1, state.savedRoutes.size)
        assertFalse(state.isSavingRoute)
    }

    @Test
    fun `saved routes opens Saved routes when signed in`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(signedIn = true)
        advanceUntilIdle()

        trip.screen.actions.onSavedRoutesClick()

        assertEquals(listOf(Call.SavedRoutes), trip.navigation.calls)
    }

    @Test
    fun `saved routes opens sign-in for Saved routes when signed out`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onSavedRoutesClick()

        assertEquals(listOf(Call.SignInForSavedRoutes), trip.navigation.calls)
    }

    private val takenRoute = FakeSavedRoutesSource.Recorded.copy(
        route = FakeSavedRoutesSource.Recorded.route.copy(id = "other-route"),
    )
}

/** A route saved under the name the planned route's destination has, so saving it asks for another name. */
private fun TestScope.planFromSignedIn(savedRoutes: List<SavedRoute> = emptyList()): TripPlanFixture =
    planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise, signedIn = true, savedRoutes = savedRoutes))

/** Plans a trip from the user's location to the covered destination the search finds, and lets it show. */
private fun TestScope.planFromPrague(trip: TripPlanFixture): TripPlanFixture {
    advanceUntilIdle()
    sendFix(trip, Points.prague)
    trip.screen.actions.onSuggestionClick(searchForCovered(trip))
    advanceUntilIdle()
    return trip
}
