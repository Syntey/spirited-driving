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
import com.syntey.spiriteddriving.data.persistence.database.DatabaseTest
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.database.SavedRouteDetailEntity
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import com.syntey.spiriteddriving.data.persistence.persistence.route.StoredRoute
import com.syntey.spiriteddriving.data.persistence.persistence.route.toStored
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

internal class SavedRoutesPersistenceTest : DatabaseTest() {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change to a full route doesn't emit the list again`() = runTest {
        val persistence = savedRoutesPersistence()
        val emissions = MutableStateFlow<List<List<SavedRoute>?>>(emptyList())
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            persistence.observe().collect { list -> emissions.update { it + listOf(list) } }
        }
        val renamed = savedRoute.copy(name = "Renamed")

        // Each write waits for its emission, so the emissions come in the order of the writes
        awaitEmissions(emissions, count = 1)
        persistence.put(savedRoute)
        awaitEmissions(emissions, count = 2)
        persistence.putRoute(SAVED_ID, fullRoute)
        persistence.put(renamed)
        awaitEmissions(emissions, count = 3)
        collecting.cancel()

        assertEquals(listOf(null, listOf(savedRoute), listOf(renamed)), emissions.value)
    }

    private suspend fun awaitEmissions(emissions: MutableStateFlow<List<List<SavedRoute>?>>, count: Int) =
        withContext(Dispatchers.Default) {
            withTimeout(5.seconds) { emissions.first { it.size >= count } }
        }

    @Test
    fun `putting a saved route with a kept ID replaces it`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.put(savedRoute)

        persistence.put(savedRoute.copy(name = "Renamed"))

        assertEquals(listOf(savedRoute.copy(name = "Renamed")), persistence.get())
    }

    @Test
    fun `put keeps the order and appends a new route at the end`() = runTest {
        val persistence = savedRoutesPersistence()
        persistence.put(savedRoute)
        persistence.put(otherRoute)

        persistence.put(savedRoute.copy(name = "Renamed"))

        assertEquals(listOf(savedRoute.copy(name = "Renamed"), otherRoute), persistence.get())
    }

    @Test
    fun `replaceAll keeps the given order`() = runTest {
        val persistence = savedRoutesPersistence()

        persistence.replaceAll(listOf(otherRoute, savedRoute))

        assertEquals(listOf(otherRoute, savedRoute), persistence.get())
    }

    @Test
    fun `a kept empty list reads as empty and not as nothing kept`() = runTest {
        val persistence = savedRoutesPersistence()

        persistence.replaceAll(emptyList())

        assertEquals(emptyList(), persistence.get())
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
    fun `an unreadable full route reads as none`() = runTest {
        val database = database()
        val persistence = savedRoutesPersistence(database = database)
        database.savedRoutesDao().upsertDetail(SavedRouteDetailEntity(savedRouteId = SAVED_ID.value, route = "garbage"))

        assertNull(persistence.getRoute(SAVED_ID))
    }

    @Test
    fun `a full route with an unknown kind reads as a fun route`() = runTest {
        val database = database()
        val json = PersistenceModule().provideJson()
        val stored = fullRoute.toStored().copy(kind = "SOMETHING_NEW")
        database.savedRoutesDao().upsertDetail(
            SavedRouteDetailEntity(
                savedRouteId = SAVED_ID.value,
                route = json.encodeToString(StoredRoute.serializer(), stored),
            ),
        )

        assertEquals(fullRoute, savedRoutesPersistence(database = database).getRoute(SAVED_ID))
    }

    @Test
    fun `replaceAll removes the legacy DataStore keys`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[LEGACY_LIST_KEY] = "[]"
            it[LEGACY_DETAILS_KEY] = "{}"
        }

        savedRoutesPersistence(dataStore).replaceAll(listOf(savedRoute))

        val preferences = dataStore.data.first()
        assertNull(preferences[LEGACY_LIST_KEY])
        assertNull(preferences[LEGACY_DETAILS_KEY])
    }

    @Test
    fun `clear removes the legacy DataStore keys`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[LEGACY_LIST_KEY] = "[]"
            it[LEGACY_DETAILS_KEY] = "{}"
        }

        savedRoutesPersistence(dataStore).clear()

        val preferences = dataStore.data.first()
        assertNull(preferences[LEGACY_LIST_KEY])
        assertNull(preferences[LEGACY_DETAILS_KEY])
    }

    private val savedRoute: SavedRoute = FakeSavedRoutesSource.Recorded
    private val otherRoute: SavedRoute = savedRoute.copy(id = OTHER_ID, name = "Other")
    private val fullRoute: Route = FakeRoutePlanner.Recorded.funRoutes.first()

    private fun savedRoutesPersistence(
        dataStore: DataStore<Preferences> = InMemoryDataStore(),
        database: PersistenceDatabase = database(),
    ): SavedRoutesPersistence {
        val json = PersistenceModule().provideJson()
        return SavedRoutesPersistence(JsonPersistence(dataStore, json), database, database.savedRoutesDao(), json)
    }

    private companion object {
        val SAVED_ID = SavedRouteId("saved-1")
        val OTHER_ID = SavedRouteId("saved-2")
        val LEGACY_LIST_KEY = stringPreferencesKey("SAVED_ROUTES")
        val LEGACY_DETAILS_KEY = stringPreferencesKey("SAVED_ROUTE_DETAILS")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = flow { emitAll(preferences) }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
