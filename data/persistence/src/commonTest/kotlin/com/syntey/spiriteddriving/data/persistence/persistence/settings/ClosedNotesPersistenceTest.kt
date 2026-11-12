package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClosedNotesPersistenceTest {

    @Test
    fun `a note is open until it is closed`() = runTest {
        val persistence = closedNotesPersistence()

        assertFalse(persistence.isClosed(Note.PreciseLocation))
    }

    @Test
    fun `closing a note keeps it closed`() = runTest {
        val persistence = closedNotesPersistence()

        persistence.close(Note.PreciseLocation)

        assertTrue(persistence.isClosed(Note.PreciseLocation))
    }

    private fun closedNotesPersistence() = ClosedNotesPersistence(PrimitivePersistence(InMemoryDataStore()))

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
