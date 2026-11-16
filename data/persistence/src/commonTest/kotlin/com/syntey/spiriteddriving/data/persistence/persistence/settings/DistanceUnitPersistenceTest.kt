package com.syntey.spiriteddriving.data.persistence.persistence.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DistanceUnitPersistenceTest {

    @Test
    fun `the unit is unpicked until the user picks one`() = runTest {
        val persistence = distanceUnitPersistence()

        assertNull(persistence.observe().first())
    }

    @Test
    fun `a picked unit is observed`() = runTest {
        val persistence = distanceUnitPersistence()

        persistence.set(DistanceUnit.Miles)

        assertEquals(DistanceUnit.Miles, persistence.observe().first())
    }

    @Test
    fun `the last picked unit is observed`() = runTest {
        val persistence = distanceUnitPersistence()

        persistence.set(DistanceUnit.Miles)
        persistence.set(DistanceUnit.Kilometres)

        assertEquals(DistanceUnit.Kilometres, persistence.observe().first())
    }

    private fun distanceUnitPersistence() = DistanceUnitPersistence(PrimitivePersistence(InMemoryDataStore()))

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
