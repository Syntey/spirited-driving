package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.drive.toPastDrive
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.data.persistence.database.DatabaseTest
import com.syntey.spiriteddriving.data.persistence.database.PastDriveRouteEntity
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.database.toEntity
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
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
import kotlin.time.Instant

internal class TripHistoryPersistenceTest : DatabaseTest() {

    @Test
    fun `nothing kept reads as null`() = runTest {
        assertNull(tripHistoryPersistence().get())
    }

    @Test
    fun `a kept page reads back with every field of both kinds and both rated and unrated drives`() = runTest {
        val persistence = tripHistoryPersistence()
        val page = page(
            drive("fastest", route = fastestRoute),
            drive("fun", rating = Rating(Thumb.Down, setOf(RatingReason.Roadworks, RatingReason.HeavyTraffic))),
            nextCursor = "2",
        )

        persistence.replaceAll(page)

        assertEquals(page, persistence.get())
    }

    @Test
    fun `replacing all drops the full routes of drives not in the page`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1"), drive("drive-2")))
        persistence.putRoute("drive-1", funRoute)
        persistence.putRoute("drive-2", fastestRoute)

        persistence.replaceAll(page(drive("drive-1")))

        assertEquals(funRoute, persistence.getRoute("drive-1"))
        assertNull(persistence.getRoute("drive-2"))
    }

    @Test
    fun `appending adds the page at the end and keeps its cursor`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1"), nextCursor = "1"))

        persistence.append(page(drive("drive-2"), nextCursor = "2"))

        assertEquals(page(drive("drive-1"), drive("drive-2"), nextCursor = "2"), persistence.get())
    }

    @Test
    fun `appending skips a drive already kept`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1"), nextCursor = "1"))

        persistence.append(page(drive("drive-1"), drive("drive-2")))

        assertEquals(listOf("drive-1", "drive-2"), persistence.get()?.drives?.map { it.id })
    }

    @Test
    fun `appending with nothing kept yet starts the list`() = runTest {
        val persistence = tripHistoryPersistence()

        persistence.append(page(drive("drive-1"), nextCursor = "1"))

        assertEquals(page(drive("drive-1"), nextCursor = "1"), persistence.get())
    }

    @Test
    fun `an unsent rating overrides the backend's rating when replacing all`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.replaceAll(page(drive("drive-1", rating = Rating(Thumb.Down))))

        assertEquals(page(drive("drive-1", rating = Rating(Thumb.Up))), persistence.get())
    }

    @Test
    fun `an unsent rating overrides the backend's rating when appending`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1"), nextCursor = "1"))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.append(page(drive("drive-1", rating = Rating(Thumb.Down)), drive("drive-2")))

        assertEquals(Rating(Thumb.Up), persistence.get()?.drives?.first()?.rating)
    }

    @Test
    fun `an unsent rating outlives a replaced page without its drive and shows when the drive is appended`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.replaceAll(page(drive("drive-2")))
        persistence.append(page(drive("drive-1", rating = Rating(Thumb.Down))))

        assertEquals(page(drive("drive-2"), drive("drive-1", rating = Rating(Thumb.Up))), persistence.get())
        assertEquals(mapOf("drive-1" to Rating(Thumb.Up)), persistence.unsentRatings())
    }

    @Test
    fun `rating a drive not kept keeps no rating`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))

        persistence.rate("drive-2", Rating(Thumb.Up))

        assertEquals(emptyMap(), persistence.unsentRatings())
    }

    @Test
    fun `rating with nothing kept keeps no rating`() = runTest {
        val persistence = tripHistoryPersistence()

        persistence.rate("drive-1", Rating(Thumb.Up))

        assertNull(persistence.get())
        assertEquals(emptyMap(), persistence.unsentRatings())
    }

    @Test
    fun `marking the same rating sent clears it as unsent`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.markRatingSent("drive-1", Rating(Thumb.Up))

        assertEquals(emptyMap(), persistence.unsentRatings())
    }

    @Test
    fun `marking a rating sent keeps it on the drive`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.markRatingSent("drive-1", Rating(Thumb.Up))

        assertEquals(page(drive("drive-1", rating = Rating(Thumb.Up))), persistence.get())
    }

    @Test
    fun `marking a different rating sent leaves the rating unsent`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.markRatingSent("drive-1", Rating(Thumb.Down))

        assertEquals(mapOf("drive-1" to Rating(Thumb.Up)), persistence.unsentRatings())
    }

    @Test
    fun `removing a drive drops it and its full route and its unsent rating`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1"), drive("drive-2")))
        persistence.putRoute("drive-1", funRoute)
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.remove("drive-1")

        assertEquals(page(drive("drive-2")), persistence.get())
        assertNull(persistence.getRoute("drive-1"))
        assertEquals(emptyMap(), persistence.unsentRatings())
    }

    @Test
    fun `clearing empties the drives and the full routes and the unsent ratings`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.putRoute("drive-1", funRoute)
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.clear()

        assertNull(persistence.get())
        assertNull(persistence.getRoute("drive-1"))
        assertEquals(emptyMap(), persistence.unsentRatings())
    }

    @Test
    fun `a full route put for a drive not in the list is kept`() = runTest {
        val persistence = tripHistoryPersistence()

        persistence.putRoute("drive-1", funRoute)

        assertEquals(funRoute, persistence.getRoute("drive-1"))
    }

    @Test
    fun `observing emits the page after each change`() = runTest {
        val persistence = tripHistoryPersistence()

        persistence.replaceAll(page(drive("drive-1")))

        assertEquals(page(drive("drive-1")), persistence.observe().first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a change to a full route doesn't emit the page again`() = runTest {
        val persistence = tripHistoryPersistence()
        val emissions = MutableStateFlow<List<PastDrivePage?>>(emptyList())
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            persistence.observe().collect { page -> emissions.update { it + listOf(page) } }
        }

        // Each write waits for its emission, so the emissions come in the order of the writes
        awaitEmissions(emissions, count = 1)
        persistence.replaceAll(page(drive("drive-1")))
        awaitEmissions(emissions, count = 2)
        persistence.putRoute("drive-1", funRoute)
        persistence.append(page(drive("drive-2")))
        awaitEmissions(emissions, count = 3)
        collecting.cancel()

        assertEquals(
            listOf(null, page(drive("drive-1")), page(drive("drive-1"), drive("drive-2"))),
            emissions.value,
        )
    }

    private suspend fun awaitEmissions(emissions: MutableStateFlow<List<PastDrivePage?>>, count: Int) =
        withContext(Dispatchers.Default) {
            withTimeout(5.seconds) { emissions.first { it.size >= count } }
        }

    @Test
    fun `an unreadable full route reads as none`() = runTest {
        val database = database()
        val persistence = tripHistoryPersistence(database = database)
        database.tripHistoryDao().upsertRoute(PastDriveRouteEntity(driveId = "drive-1", route = "garbage"))

        assertNull(persistence.getRoute("drive-1"))
    }

    @Test
    fun `an unreadable rating reads as no rating`() = runTest {
        val database = database()
        val persistence = tripHistoryPersistence(database = database)
        persistence.replaceAll(page(drive("drive-1", rating = Rating(Thumb.Up))))
        database.tripHistoryDao().setRating("drive-1", "garbage")

        assertEquals(page(drive("drive-1")), persistence.get())
    }

    @Test
    fun `a drive with an unknown kind reads as a fun drive`() = runTest {
        val database = database()
        val persistence = tripHistoryPersistence(database = database)
        val stored = drive("drive-1").toEntity(position = 0, rating = null).copy(kind = "SOMETHING_NEW")
        database.tripHistoryDao().replaceAll(drives = listOf(stored), nextCursor = null)

        assertEquals(drive("drive-1"), persistence.get()?.drives?.single())
    }

    @Test
    fun `replaceAll removes the legacy DataStore keys`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[LEGACY_LIST_KEY] = "{}"
            it[LEGACY_ROUTES_KEY] = "{}"
            it[LEGACY_UNSENT_KEY] = "{}"
        }

        tripHistoryPersistence(dataStore).replaceAll(page(drive("drive-1")))

        val preferences = dataStore.data.first()
        assertNull(preferences[LEGACY_LIST_KEY])
        assertNull(preferences[LEGACY_ROUTES_KEY])
        assertNull(preferences[LEGACY_UNSENT_KEY])
    }

    @Test
    fun `clear removes the legacy DataStore keys`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[LEGACY_LIST_KEY] = "{}"
            it[LEGACY_ROUTES_KEY] = "{}"
            it[LEGACY_UNSENT_KEY] = "{}"
        }

        tripHistoryPersistence(dataStore).clear()

        val preferences = dataStore.data.first()
        assertNull(preferences[LEGACY_LIST_KEY])
        assertNull(preferences[LEGACY_ROUTES_KEY])
        assertNull(preferences[LEGACY_UNSENT_KEY])
    }

    private val fastestRoute: Route = FakeRoutePlanner.Recorded.fastest
    private val funRoute: Route = FakeRoutePlanner.Recorded.funRoutes.first()

    private fun drive(id: String, route: Route = funRoute, rating: Rating? = null): PastDrive =
        route.toPastDrive(id = id, startedAt = STARTED_AT, rating = rating)

    private fun page(vararg drives: PastDrive, nextCursor: String? = null) = PastDrivePage(drives.toList(), nextCursor)

    private fun tripHistoryPersistence(
        dataStore: DataStore<Preferences> = InMemoryDataStore(),
        database: PersistenceDatabase = database(),
    ): TripHistoryPersistence {
        val json = PersistenceModule().provideJson()
        return TripHistoryPersistence(
            jsonPersistence = JsonPersistence(dataStore, json),
            database = database,
            tripHistoryDao = database.tripHistoryDao(),
            json = json,
        )
    }

    private companion object {
        val STARTED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")
        val LEGACY_LIST_KEY = stringPreferencesKey("TRIP_HISTORY")
        val LEGACY_ROUTES_KEY = stringPreferencesKey("TRIP_HISTORY_ROUTES")
        val LEGACY_UNSENT_KEY = stringPreferencesKey("TRIP_HISTORY_UNSENT_RATINGS")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = flow { emitAll(preferences) }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
