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
import kotlin.time.Instant

class TripHistoryPersistenceTest {

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
    fun `rating a drive not kept keeps no rating`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))

        persistence.rate("drive-2", Rating(Thumb.Up))

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
    fun `marking a different rating sent leaves the rating unsent`() = runTest {
        val persistence = tripHistoryPersistence()
        persistence.replaceAll(page(drive("drive-1")))
        persistence.rate("drive-1", Rating(Thumb.Up))

        persistence.markRatingSent("drive-1", Rating(Thumb.Down))

        assertEquals(mapOf("drive-1" to Rating(Thumb.Up)), persistence.unsentRatings())
    }

    @Test
    fun `removing a drive drops it, its full route and its unsent rating`() = runTest {
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
    fun `clearing empties the drives, the full routes and the unsent ratings`() = runTest {
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
    fun `observing emits the page after each change`() = runTest {
        val persistence = tripHistoryPersistence()

        persistence.replaceAll(page(drive("drive-1")))

        assertEquals(page(drive("drive-1")), persistence.observe().first())
    }

    @Test
    fun `an invalid copy of the trip history reads as nothing kept`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[TRIP_HISTORY_KEY] = "garbage" }

        assertNull(tripHistoryPersistence(dataStore).get())
    }

    @Test
    fun `an invalid copy of the full routes reads as no route`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[TRIP_HISTORY_ROUTES_KEY] = "garbage" }

        assertNull(tripHistoryPersistence(dataStore).getRoute("drive-1"))
    }

    @Test
    fun `a drive with an unknown kind reads as a fun drive`() = runTest {
        val jsonPersistence = JsonPersistence(InMemoryDataStore(), PersistenceModule().provideJson())
        val stored = drive("drive-1").toStored().copy(kind = "SOMETHING_NEW")
        jsonPersistence.save(TRIP_HISTORY_KEY, StoredTripHistory(drives = listOf(stored), nextCursor = null))

        assertEquals(drive("drive-1"), TripHistoryPersistence(jsonPersistence).get()?.drives?.single())
    }

    private val fastestRoute: Route = FakeRoutePlanner.Recorded.fastest
    private val funRoute: Route = FakeRoutePlanner.Recorded.funRoutes.first()

    private fun drive(id: String, route: Route = funRoute, rating: Rating? = null): PastDrive =
        route.toPastDrive(id = id, startedAt = STARTED_AT, rating = rating)

    private fun page(vararg drives: PastDrive, nextCursor: String? = null) = PastDrivePage(drives.toList(), nextCursor)

    private fun tripHistoryPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        TripHistoryPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private companion object {
        val STARTED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")
        val TRIP_HISTORY_KEY = stringPreferencesKey("TRIP_HISTORY")
        val TRIP_HISTORY_ROUTES_KEY = stringPreferencesKey("TRIP_HISTORY_ROUTES")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
