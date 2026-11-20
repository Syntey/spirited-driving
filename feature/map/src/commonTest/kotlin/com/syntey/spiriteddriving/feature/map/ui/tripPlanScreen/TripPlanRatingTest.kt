package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
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

class TripPlanRatingTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `a thumb shows the reasons of that thumb only`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertEquals(RatingReason.of(Thumb.Up), trip.screen.viewState.value.reasonChips.map { it.reason })
    }

    @Test
    fun `a reason of the other thumb can't be picked`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.screen.actions.onRatingReasonClick(RatingReason.BoringRoads)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Up), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `a reason of the picked thumb is selected`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.screen.actions.onRatingReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        val chip = trip.screen.viewState.value.reasonChips.first { it.reason == RatingReason.CurvyRoads }
        assertTrue(chip.isSelected)
    }

    @Test
    fun `the same thumb again keeps the reasons picked`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        trip.screen.actions.onRatingReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads)), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `the other thumb replaces the first thumb and its reasons`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        trip.screen.actions.onRatingReasonClick(RatingReason.CurvyRoads)
        advanceUntilIdle()

        trip.screen.actions.onRatingThumbClick(Thumb.Down)
        advanceUntilIdle()

        assertEquals(Rating(Thumb.Down), trip.screen.viewState.value.ratingStep?.rating)
    }

    @Test
    fun `the reminder offer shows after the first thumb`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        assertFalse(trip.screen.viewState.value.showsReminderOffer)

        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertTrue(trip.screen.viewState.value.showsReminderOffer)
    }

    @Test
    fun `the reminder offer isn't shown again for a later drive`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()
        trip.screen.actions.onRatingDone()
        trip.openDriveQuestion.set(driveQuestion(startedAgo = 15.minutes))
        returnToScreen(trip)

        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        assertFalse(trip.screen.viewState.value.showsReminderOffer)
    }

    @Test
    fun `turning reminders on from the offer hides the offer`() = runTest(main.dispatcher) {
        val trip = ratingTrip()
        trip.screen.actions.onRatingThumbClick(Thumb.Up)
        advanceUntilIdle()

        trip.screen.actions.onReminderOfferClick()
        advanceUntilIdle()

        assertFalse(trip.screen.viewState.value.showsReminderOffer)
    }

    @Test
    fun `not driven closes the rating step and the drive isn't asked about again`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.screen.actions.onNotDrivenClick()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.ratingStep)
        assertEquals(SheetHeight.Collapsed, trip.screen.viewState.value.sheetHeight)

        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `closing the rating step without a thumb skips its question`() = runTest(main.dispatcher) {
        val trip = ratingTrip()

        trip.screen.actions.onRatingClose()
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
        assertNull(state.planning)
        assertNull(state.startSearch)
        assertEquals("drive", state.ratingStep?.question?.driveId)
    }

    private fun TestScope.ratingTrip(): TripPlanFixture {
        val trip = tripPlanFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes), canOfferReminders = true)
        advanceUntilIdle()
        return trip
    }
}
