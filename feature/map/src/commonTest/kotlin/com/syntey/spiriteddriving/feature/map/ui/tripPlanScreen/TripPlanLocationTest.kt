package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TripPlanLocationTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `access turned on in the system settings is read when the screen starts again`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        assertEquals(LocationAccess.Denied, trip.screen.viewState.value.locationAccess)

        trip.locationPermission.access = LocationAccess.Precise
        returnToScreen(trip)

        val state = trip.screen.viewState.value
        assertEquals(LocationAccess.Precise, state.locationAccess)
        assertEquals(LocateButton.Following, state.locateButton)
    }

    @Test
    fun `the locate button shows following while the map follows the user`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()

        assertEquals(LocateButton.Following, trip.screen.viewState.value.locateButton)
    }

    @Test
    fun `the locate button shows not following once the map is moved by hand`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()

        trip.screen.actions.onMapPanned()
        advanceUntilIdle()

        assertEquals(LocateButton.NotFollowing, trip.screen.viewState.value.locateButton)
    }

    @Test
    fun `tapping the locate button when not following follows the user again`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        trip.screen.actions.onMapPanned()
        advanceUntilIdle()

        trip.screen.actions.onLocateClick()
        advanceUntilIdle()

        assertEquals(LocateButton.Following, trip.screen.viewState.value.locateButton)
    }

    @Test
    fun `the locate button without access asks for it and follows once it's given`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        assertEquals(LocateButton.NoAccess, trip.screen.viewState.value.locateButton)
        trip.locationPermission.access = LocationAccess.Precise

        trip.screen.actions.onLocateClick()
        advanceUntilIdle()

        assertEquals(LocateButton.Following, trip.screen.viewState.value.locateButton)
    }

    @Test
    fun `the locate button with access denied for good doesn't ask again`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.DeniedForGood)
        advanceUntilIdle()
        assertEquals(LocateButton.NoAccess, trip.screen.viewState.value.locateButton)
        // A prompt would give access now; only the system settings can, so the access stays denied
        trip.locationPermission.access = LocationAccess.Precise

        trip.screen.actions.onLocateClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(LocationAccess.DeniedForGood, state.locationAccess)
        assertEquals(LocateButton.NoAccess, state.locateButton)
    }
}
