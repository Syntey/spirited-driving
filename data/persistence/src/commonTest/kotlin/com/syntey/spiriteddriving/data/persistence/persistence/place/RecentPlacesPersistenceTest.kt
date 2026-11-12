package com.syntey.spiriteddriving.data.persistence.persistence.place

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentPlacesPersistenceTest {

    @Test
    fun `nothing kept reads as empty`() = runTest {
        assertTrue(recentPlacesPersistence().get().isEmpty())
    }

    @Test
    fun `a kept list reads back equal`() = runTest {
        val persistence = recentPlacesPersistence()
        val places = listOf(samplePlace(), samplePlace().copy(name = "Dresden", area = null))

        persistence.update { places }

        assertEquals(places, persistence.get())
    }

    @Test
    fun `an unnamed place reads back with no name`() = runTest {
        val persistence = recentPlacesPersistence()
        val unnamed = samplePlace().copy(name = null)

        persistence.update { listOf(unnamed) }

        assertEquals(listOf(unnamed), persistence.get())
    }

    @Test
    fun `a list stored before names were optional still reads`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[RECENT_PLACES_KEY] =
                """[{"name":"Karlštejn","area":"okres Beroun, Central Bohemian Region","location":{"lat":49.933809,"lon":14.1832917}}]"""
        }

        assertEquals(listOf(samplePlace()), recentPlacesPersistence(dataStore).get())
    }

    @Test
    fun `update gets the current list`() = runTest {
        val persistence = recentPlacesPersistence()
        persistence.update { listOf(samplePlace()) }
        var seen: List<Place>? = null

        persistence.update { current ->
            seen = current
            current + samplePlace().copy(name = "Dresden")
        }

        assertEquals(listOf(samplePlace()), seen)
    }

    @Test
    fun `an invalid copy reads as empty`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[RECENT_PLACES_KEY] = "garbage" }

        assertTrue(recentPlacesPersistence(dataStore).get().isEmpty())
    }

    @Test
    fun `update starts from empty when the copy is invalid`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[RECENT_PLACES_KEY] = "garbage" }
        val persistence = recentPlacesPersistence(dataStore)

        val updated = persistence.update { current -> current + samplePlace() }

        assertEquals(listOf(samplePlace()), updated)
    }

    private fun recentPlacesPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        RecentPlacesPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private fun samplePlace() = Place(
        name = "Karlštejn",
        area = "okres Beroun, Central Bohemian Region",
        location = GeoPoint(latitude = 49.933809, longitude = 14.1832917),
    )

    private companion object {
        val RECENT_PLACES_KEY = stringPreferencesKey("RECENT_PLACES")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
