package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CloseNoteUseCaseTest {

    @Test
    fun `closes the note it's given`() = runTest {
        val closedNotes = RecordingClosedNotes()

        CloseNoteUseCase(closedNotes).build(Note.PreciseLocation)

        assertEquals(listOf(Note.PreciseLocation), closedNotes.closed)
    }

    private class RecordingClosedNotes : ClosedNotes {
        val closed = mutableListOf<Note>()

        override suspend fun isClosed(note: Note): Boolean = note in closed
        override suspend fun close(note: Note) {
            closed += note
        }
    }
}
