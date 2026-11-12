package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import org.koin.core.annotation.Single

/**
 * One key per note, so a note without a key is one the user hasn't closed.
 */
@Single
internal class ClosedNotesPersistence(private val primitivePersistence: PrimitivePersistence) : ClosedNotes {

    override suspend fun isClosed(note: Note): Boolean = primitivePersistence.get(key(note)) ?: false

    override suspend fun close(note: Note) {
        primitivePersistence.save(key(note), true)
    }

    private fun key(note: Note) = booleanPreferencesKey("NOTE_CLOSED_${note.name}")
}
