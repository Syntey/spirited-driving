package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note

internal class RecordingClosedNotes(closed: Set<Note> = emptySet()) : ClosedNotes {
    var closed: Set<Note> = closed
        private set

    override suspend fun isClosed(note: Note): Boolean = note in closed
    override suspend fun close(note: Note) {
        closed += note
    }
}
