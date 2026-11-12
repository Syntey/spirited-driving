package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsNoteClosedUseCaseTest {

    @Test
    fun `a note is open until it is closed`() = runTest {
        val useCase = IsNoteClosedUseCase(FakeClosedNotes())

        assertFalse(useCase.build(Note.PreciseLocation))
    }

    @Test
    fun `a closed note is closed`() = runTest {
        val closedNotes = FakeClosedNotes(setOf(Note.PreciseLocation))

        assertTrue(IsNoteClosedUseCase(closedNotes).build(Note.PreciseLocation))
    }

    private class FakeClosedNotes(private val closed: Set<Note> = emptySet()) : ClosedNotes {
        override suspend fun isClosed(note: Note): Boolean = note in closed
        override suspend fun close(note: Note) = Unit
    }
}
