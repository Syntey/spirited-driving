package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import androidx.datastore.preferences.core.stringPreferencesKey

class StartedDrivesPersistenceTest {

    @Test
    fun `nothing kept reads as empty`() = runTest {
        assertTrue(startedDrivesPersistence().all().isEmpty())
    }

    @Test
    fun `an added drive is read first`() = runTest {
        val persistence = startedDrivesPersistence()

        persistence.add(sampleDrive(id = "first"))
        persistence.add(sampleDrive(id = "second"))

        assertEquals(listOf("second", "first"), persistence.all().map { it.id })
    }

    @Test
    fun `adding a drive again moves it to the front without a copy`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.add(sampleDrive(id = "second"))

        persistence.add(sampleDrive(id = "first"))

        assertEquals(listOf("first", "second"), persistence.all().map { it.id })
    }

    @Test
    fun `adding past the limit drops the oldest drive`() = runTest {
        val persistence = startedDrivesPersistence()

        repeat(101) { persistence.add(sampleDrive(id = "drive-$it")) }

        val kept = persistence.all()
        assertEquals(100, kept.size)
        assertEquals("drive-100", kept.first().id)
        assertEquals("drive-1", kept.last().id)
    }

    @Test
    fun `marking a drive recorded keeps the others as they are`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.add(sampleDrive(id = "second"))

        persistence.markRecorded("first")

        assertEquals(listOf(sampleDrive(id = "second"), sampleDrive(id = "first", isRecorded = true)), persistence.all())
    }

    @Test
    fun `marking an unknown drive recorded changes nothing`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))

        persistence.markRecorded("unknown")

        assertEquals(listOf(sampleDrive(id = "first")), persistence.all())
    }

    @Test
    fun `removing a drive leaves the others`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.add(sampleDrive(id = "second"))

        persistence.remove("second")

        assertEquals(listOf(sampleDrive(id = "first")), persistence.all())
    }

    @Test
    fun `a kept drive reads back with every field`() = runTest {
        val persistence = startedDrivesPersistence()
        val drive = Drive(
            id = "3f2b8c1e-5d4a-4e7b-9a6c-1d2e3f4a5b6c",
            routeId = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
            startedAt = Instant.parse("2026-11-13T10:15:30.250Z"),
            plannedDuration = 3900.seconds,
            isRecorded = true,
        )

        persistence.add(drive)

        assertEquals(listOf(drive), persistence.all())
    }

    @Test
    fun `an invalid copy reads as empty`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[STARTED_DRIVES_KEY] = "garbage" }

        assertTrue(startedDrivesPersistence(dataStore).all().isEmpty())
    }

    @Test
    fun `adding to an invalid copy starts from empty`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[STARTED_DRIVES_KEY] = "garbage" }
        val persistence = startedDrivesPersistence(dataStore)

        persistence.add(sampleDrive(id = "first"))

        assertEquals(listOf(sampleDrive(id = "first")), persistence.all())
    }

    @Test
    fun `a rated drive keeps its rating unsent`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))

        persistence.rate("first", Rating(Thumb.Up))

        assertEquals(listOf(sampleDrive(id = "first").copy(rating = Rating(Thumb.Up))), persistence.all())
    }

    @Test
    fun `marking the rating sent marks it sent`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.rate("first", Rating(Thumb.Up))

        persistence.markRatingSent("first", Rating(Thumb.Up))

        assertEquals(true, persistence.all().single().isRatingSent)
    }

    @Test
    fun `marking a different rating sent leaves the rating unsent`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.rate("first", Rating(Thumb.Up))

        persistence.markRatingSent("first", Rating(Thumb.Down))

        assertEquals(false, persistence.all().single().isRatingSent)
    }

    @Test
    fun `marking a drive not driven keeps the others as they are`() = runTest {
        val persistence = startedDrivesPersistence()
        persistence.add(sampleDrive(id = "first"))
        persistence.add(sampleDrive(id = "second"))

        persistence.markNotDriven("second")

        assertEquals(listOf(sampleDrive(id = "second", isNotDriven = true), sampleDrive(id = "first")), persistence.all())
    }

    @Test
    fun `a rating with reasons reads back with the same reasons`() = runTest {
        val persistence = startedDrivesPersistence()
        val rating = Rating(Thumb.Down, setOf(RatingReason.Roadworks, RatingReason.HeavyTraffic))
        persistence.add(sampleDrive(id = "first"))

        persistence.rate("first", rating)

        assertEquals(rating, persistence.all().single().rating)
    }

    @Test
    fun `a drive stored before ratings existed still reads`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit {
            it[STARTED_DRIVES_KEY] =
                """[{"id":"first","routeId":"9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d","startedAtEpochMillis":1794564930000,"plannedDurationSeconds":3900,"isRecorded":false}]"""
        }

        assertEquals(listOf(sampleDrive(id = "first")), startedDrivesPersistence(dataStore).all())
    }

    private fun startedDrivesPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        StartedDrivesPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private fun sampleDrive(id: String, isRecorded: Boolean = false, isNotDriven: Boolean = false) = Drive(
        id = id,
        routeId = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
        startedAt = Instant.parse("2026-11-13T10:15:30Z"),
        plannedDuration = 3900.seconds,
        isRecorded = isRecorded,
        isNotDriven = isNotDriven,
    )

    private companion object {
        val STARTED_DRIVES_KEY = stringPreferencesKey("STARTED_DRIVES")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
