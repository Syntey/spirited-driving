package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
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

class MapSheetRatingTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `a thumb shows the reasons of that thumb only`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertEquals(RatingReason.of(Thumb.Up), trip.rating.viewState.value.reasonChips.map { it.reason })
    }

    @Test
    fun `a reason of the other thumb can't be picked`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.rating.actions.onReasonClick(RatingReason.BoringRoads)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Up), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `a reason of the picked thumb is selected`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.rating.actions.onReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        val chip = trip.rating.viewState.value.reasonChips.first { it.reason == RatingReason.CurvyRoads }
        assertTrue(chip.isSelected)
    }

    @Test
    fun `the same thumb again keeps the reasons picked`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        trip.rating.actions.onReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads)), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `the other thumb replaces the first thumb and its reasons`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        trip.rating.actions.onReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        trip.rating.actions.onThumbClick(Thumb.Down)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Down), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `the reminder offer shows after the first thumb`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        assertFalse(trip.rating.viewState.value.showsReminderOffer)

        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertTrue(trip.rating.viewState.value.showsReminderOffer)
    }

    @Test
    fun `the reminder offer isn't shown again for a later drive`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()
        trip.rating.actions.onDoneClick()
        trip.openDriveQuestion.set(driveQuestion(startedAgo = 15.minutes))
        returnToScreen(trip)

        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertFalse(trip.rating.viewState.value.showsReminderOffer)
    }

    @Test
    fun `turning reminders on from the offer hides the offer`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.rating.actions.onThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.rating.actions.onReminderOfferClick()
        advanceUntilIdle()

        assertFalse(trip.rating.viewState.value.showsReminderOffer)
    }

    @Test
    fun `not driven closes the rating step and the drive isn't asked about again`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.rating.actions.onNotDrivenClick()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.ratingStep)
        assertEquals(SheetHeight.Collapsed, trip.screen.viewState.value.sheetHeight)

        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `closing the rating step without a thumb skips its question`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.rating.actions.onCloseClick()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.ratingStep)

        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `touching and holding the map does nothing during the rating step`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        assertNotNull(trip.screen.viewState.value.ratingStep)

        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(trip.planningState)
        assertNull(state.startSearch)
        assertEquals("drive", state.ratingStep?.question?.driveId)
    }

    @Test
    fun `a saved route chosen during the rating step replaces it and skips its question`() = runTest(main.dispatcher) {
        val trip = ratingTrip(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        trip.screen.showSavedRoute(FakeSavedRoutesSource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNotNull(state.oneRouteStep)
        assertNull(state.ratingStep)
        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `a past drive chosen during the rating step replaces it and skips its question`() = runTest(main.dispatcher) {
        val trip = ratingTrip(pastDrives = listOf(FakeTripHistorySource.Recorded))

        trip.screen.showPastDrive(FakeTripHistorySource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNotNull(state.oneRouteStep)
        assertNull(state.ratingStep)
        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `planning new routes during the rating step replaces it and skips its question`() = runTest(main.dispatcher) {
        val trip = ratingTrip(locationAccess = LocationAccess.Precise, pastDrives = listOf(FakeTripHistorySource.Recorded))

        trip.screen.planNewRoutes(FakeTripHistorySource.Recorded.id)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNotNull(trip.planningState)
        assertNull(state.ratingStep)
        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    private fun TestScope.ratingTrip(
        locationAccess: LocationAccess = LocationAccess.Denied,
        savedRoutes: List<SavedRoute> = emptyList(),
        pastDrives: List<PastDrive> = emptyList(),
    ): MapSheetFixture {
        val trip = mapSheetFixture(
            locationAccess = locationAccess,
            savedRoutes = savedRoutes,
            pastDrives = pastDrives,
            driveQuestion = driveQuestion(startedAgo = 15.minutes),
            canOfferReminders = true,
        )
        advanceUntilIdle()
        return trip
    }
}
