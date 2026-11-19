package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.persistence.route.toStored
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedRoutesPersistenceTest {

    @Test
    fun `nothing kept reads as null`() = runTest {
        assertNull(savedRoutesPersistence().get())
    }

    @Test
    fun `a kept saved route reads back with every field`() = runTest {
        val persistence = savedRoutesPersistence()

        persistence.put(savedRoute)

        assertEquals(listOf(savedRoute), persistence.get())
    }

    @Test
    fun `a kept full route reads back with every field`() = runTest {
        val persistence = savedRoutesPersistence()

        persistence.putRoute(SAVED_ID, fullRoute)

        assertEquals(fullRoute, persistence.getRoute(SAVED_ID))
    }

    @Test
    fun `observe emits the list after a put`() = runTest {
        val persistence = savedRoutesPersistence()

        persistence.put(savedRoute)

        assertEquals(listOf(savedRoute), persistence.observe().first())
    }

    @Test
    fun `putting a saved route with a kept ID replaces it`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.put(savedRoute)

        persistence.put(savedRoute.copy(name = "Renamed"))

        assertEquals(listOf(savedRoute.copy(name = "Renamed")), persistence.get())
    }

    @Test
    fun `replacing all drops the full routes no longer in the list`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.putRoute(SAVED_ID, fullRoute)
        persistence.putRoute(OTHER_ID, fullRoute)

        persistence.replaceAll(listOf(savedRoute))

        assertEquals(listOf(savedRoute), persistence.get())
        assertEquals(fullRoute, persistence.getRoute(SAVED_ID))
        assertNull(persistence.getRoute(OTHER_ID))
    }

    @Test
    fun `remove drops the saved route and its full route`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.put(savedRoute)
        persistence.putRoute(SAVED_ID, fullRoute)

        persistence.remove(SAVED_ID)

        assertEquals(emptyList(), persistence.get())
        assertNull(persistence.getRoute(SAVED_ID))
    }

    @Test
    fun `clear empties the list and the full routes`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.put(savedRoute)
        persistence.putRoute(SAVED_ID, fullRoute)

        persistence.clear()

        assertNull(persistence.get())
        assertNull(persistence.getRoute(SAVED_ID))
    }

    @Test
    fun `an invalid copy of the list reads as nothing kept`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[SAVED_ROUTES_KEY] = "garbage" }

        assertNull(savedRoutesPersistence(dataStore).get())
    }

    @Test
    fun `a full route with an unknown kind reads as a fun route`() = runTest {
        val jsonPersistence = JsonPersistence(InMemoryDataStore(), PersistenceModule().provideJson())
        val stored = fullRoute.toStored().copy(kind = "SOMETHING_NEW")
        jsonPersistence.save(SAVED_ROUTE_DETAILS_KEY, mapOf(SAVED_ID.value to stored))

        assertEquals(fullRoute, SavedRoutesPersistence(jsonPersistence).getRoute(SAVED_ID))
    }

    private val savedRoute: SavedRoute = FakeSavedRoutesSource.Recorded
    private val fullRoute: Route = FakeRoutePlanner.Recorded.funRoutes.first()

    private fun savedRoutesPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        SavedRoutesPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private companion object {
        val SAVED_ID = SavedRouteId("saved-1")
        val OTHER_ID = SavedRouteId("saved-2")
        val SAVED_ROUTES_KEY = stringPreferencesKey("SAVED_ROUTES")
        val SAVED_ROUTE_DETAILS_KEY = stringPreferencesKey("SAVED_ROUTE_DETAILS")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
