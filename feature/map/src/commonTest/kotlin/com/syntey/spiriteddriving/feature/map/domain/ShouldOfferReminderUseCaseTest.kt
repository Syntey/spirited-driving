package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShouldOfferReminderUseCaseTest {

    @Test
    fun `offers the reminder and closes its note when the phone can offer it`() = runTest {
        val closedNotes = RecordingClosedNotes()

        val shouldOffer = useCase(closedNotes, canOffer = true).build(Unit)

        assertTrue(shouldOffer)
        assertEquals(setOf(Note.ReminderOffer), closedNotes.closed)
    }

    @Test
    fun `doesn't offer the reminder again once its note is closed`() = runTest {
        val closedNotes = RecordingClosedNotes(closed = setOf(Note.ReminderOffer))

        val shouldOffer = useCase(closedNotes, canOffer = true).build(Unit)

        assertFalse(shouldOffer)
    }

    @Test
    fun `doesn't offer the reminder and leaves its note open when the phone can't offer it`() = runTest {
        val closedNotes = RecordingClosedNotes()

        val shouldOffer = useCase(closedNotes, canOffer = false).build(Unit)

        assertFalse(shouldOffer)
        assertTrue(closedNotes.closed.isEmpty())
    }

    private fun useCase(closedNotes: ClosedNotes, canOffer: Boolean) = ShouldOfferReminderUseCase(
        closedNotes,
        DriveReminders(FakePlatformDriveReminders(canOffer = canOffer)),
    )
}
