package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.tools.Constants
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class TripPlanDriveTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `the first drive asks with a dialog before it hands off`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))

        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(DriveDialog.FirstDrive, state.driveDialog)
        assertEquals(false, state.planning?.isHandedOff)
    }

    @Test
    fun `confirming the first-drive dialog hands the route off`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))
        trip.screen.actions.onDriveClick()

        trip.screen.actions.onFirstDriveConfirm()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.driveDialog)
        assertEquals(true, state.planning?.isHandedOff)
    }

    @Test
    fun `a drive after the first-drive dialog was confirmed hands off without the dialog`() = runTest(main.dispatcher) {
        val trip = planFromPrague(tripPlanFixture(locationAccess = LocationAccess.Precise))
        trip.screen.actions.onDriveClick()
        trip.screen.actions.onFirstDriveConfirm()
        advanceUntilIdle()

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()
        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.driveDialog)
        assertEquals(true, state.planning?.isHandedOff)
    }

    @Test
    fun `a drive with the first-drive note already closed hands off without the dialog`() = runTest(main.dispatcher) {
        val trip = planFromPrague(
            tripPlanFixture(locationAccess = LocationAccess.Precise, initiallyClosed = setOf(Note.FirstDrive)),
        )

        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.driveDialog)
        assertEquals(true, state.planning?.isHandedOff)
    }

    @Test
    fun `a missing maps app shows its dialog and doesn't hand off`() = runTest(main.dispatcher) {
        val trip = planFromPrague(
            tripPlanFixture(
                locationAccess = LocationAccess.Precise,
                initiallyClosed = setOf(Note.FirstDrive),
                mapsAppResult = MapsAppResult.Missing,
            ),
        )

        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(DriveDialog.MapsAppMissing, state.driveDialog)
        assertEquals(false, state.planning?.isHandedOff)
    }

    @Test
    fun `the store opened from the maps app dialog closes the dialog`() = runTest(main.dispatcher) {
        val trip = planFromPrague(
            tripPlanFixture(
                locationAccess = LocationAccess.Precise,
                initiallyClosed = setOf(Note.FirstDrive),
                mapsAppResult = MapsAppResult.Missing,
            ),
        )
        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        trip.screen.actions.onMapsAppStoreClick()
        advanceUntilIdle()

        assertNull(trip.screen.viewState.value.driveDialog)
    }

    @Test
    fun `a hand-off clears the reminder about an earlier drive`() = runTest(main.dispatcher) {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        // Past the rating step's window, so the question shows as the reminder bubble
        val question = DriveQuestion(
            driveId = "earlier-drive",
            startedAt = Clock.System.now() - route.duration - Constants.DriveFeedback.ASKS_UNTIL_AFTER_ARRIVAL - 1.hours,
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination,
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        val trip = tripPlanFixture(
            locationAccess = LocationAccess.Precise,
            initiallyClosed = setOf(Note.FirstDrive),
            driveQuestion = question,
        )
        advanceUntilIdle()
        assertNotNull(trip.screen.viewState.value.reminder)
        sendFix(trip, Points.prague)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.reminder)
        assertEquals(true, state.planning?.isHandedOff)
    }

    @Test
    fun `a hand-off from a saved route's step marks the step handed off`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(
            locationAccess = LocationAccess.Precise,
            initiallyClosed = setOf(Note.FirstDrive),
            savedRoutes = listOf(FakeSavedRoutesSource.Recorded),
        )
        advanceUntilIdle()
        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        trip.screen.actions.onDriveClick()
        advanceUntilIdle()

        assertEquals(true, trip.screen.viewState.value.oneRouteStep?.isHandedOff)
    }
}

/** Plans a trip from the user's location to the covered destination the search finds, and lets it show. */
private fun TestScope.planFromPrague(trip: TripPlanFixture): TripPlanFixture {
    advanceUntilIdle()
    sendFix(trip, Points.prague)
    trip.screen.actions.onSuggestionClick(searchForCovered(trip))
    advanceUntilIdle()
    return trip
}
