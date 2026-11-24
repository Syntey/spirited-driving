package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MapSheetDriveQuestionTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `a drive handed off earlier opens its rating step on start`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()

        val step = trip.screen.viewState.value.ratingStep
        assertEquals("drive", step?.question?.driveId)
    }

    @Test
    fun `a rating step is not opened when the drive's question is still too early`() = runTest(main.dispatcher) {
        // Half the planned duration has not passed yet, so the drive is likely not over
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 2.minutes))
        advanceUntilIdle()

        assertNull(trip.screen.viewState.value.ratingStep)
        assertNull(trip.screen.viewState.value.reminder)
    }

    @Test
    fun `a due rating step replaces a trip planned after the hand-off`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()
        trip.openDriveQuestion.set(driveQuestion(startedAgo = 15.minutes))

        returnToScreen(trip)

        val state = trip.screen.viewState.value
        assertEquals("drive", state.ratingStep?.question?.driveId)
        assertNull(trip.planningState)
    }

    @Test
    fun `an old drive's question shows as the reminder bubble on the map`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 5.hours))
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.ratingStep)
        assertTrue(state.showsReminder)
    }

    @Test
    fun `dismissing the reminder bubble skips the drive's question`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 5.hours))
        advanceUntilIdle()

        trip.screen.actions.onReminderDismiss()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.reminder)

        returnToScreen(trip)
        assertNull(trip.screen.viewState.value.reminder)
        assertNull(trip.screen.viewState.value.ratingStep)
    }

    @Test
    fun `an opened notification opens the drive's rating step over planning`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onMapLongPress(Points.pinned)
        advanceUntilIdle()
        trip.openDriveQuestion.set(driveQuestion(startedAgo = 15.minutes))

        trip.driveReminders.tap("drive")
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals("drive", state.ratingStep?.question?.driveId)
        assertNull(trip.planningState)
    }

    @Test
    fun `an opened notification keeps a rating step that is already open`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(driveQuestion = driveQuestion(startedAgo = 15.minutes))
        advanceUntilIdle()
        // A rating step opened afresh would pull the sheet back to half, so a full sheet shows it stayed
        trip.screen.actions.onSheetHeightChange(SheetHeight.Full)

        trip.driveReminders.tap("drive")
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals("drive", state.ratingStep?.question?.driveId)
        assertEquals(SheetHeight.Full, state.sheetHeight)
    }
}
