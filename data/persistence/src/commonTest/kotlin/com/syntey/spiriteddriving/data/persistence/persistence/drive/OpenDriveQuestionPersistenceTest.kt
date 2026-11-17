package com.syntey.spiriteddriving.data.persistence.persistence.drive

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class OpenDriveQuestionPersistenceTest {

    @Test
    fun `nothing kept reads as none`() = runTest {
        assertNull(openDriveQuestionPersistence().get())
    }

    @Test
    fun `a kept question reads back with every field`() = runTest {
        val persistence = openDriveQuestionPersistence()

        persistence.set(sampleQuestion())

        assertEquals(sampleQuestion(), persistence.get())
    }

    @Test
    fun `setting a question replaces the one kept`() = runTest {
        val persistence = openDriveQuestionPersistence()
        persistence.set(sampleQuestion(driveId = "first"))

        persistence.set(sampleQuestion(driveId = "second"))

        assertEquals("second", persistence.get()?.driveId)
    }

    @Test
    fun `clearing another drive keeps the question`() = runTest {
        val persistence = openDriveQuestionPersistence()
        persistence.set(sampleQuestion(driveId = "first"))

        persistence.clear("second")

        assertEquals(sampleQuestion(driveId = "first"), persistence.get())
    }

    @Test
    fun `clearing the drive drops the question`() = runTest {
        val persistence = openDriveQuestionPersistence()
        persistence.set(sampleQuestion(driveId = "first"))

        persistence.clear("first")

        assertNull(persistence.get())
    }

    private fun openDriveQuestionPersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        OpenDriveQuestionPersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private fun sampleQuestion(driveId: String = "first") = DriveQuestion(
        driveId = driveId,
        startedAt = Instant.parse("2026-11-13T10:15:30.250Z"),
        routeNumber = 3,
        start = GeoPoint(latitude = 49.933809, longitude = 14.1832917),
        destination = Place(name = "Dresden", area = null, location = GeoPoint(latitude = 51.0504, longitude = 13.7373)),
        duration = 3900.seconds,
        distance = 123_456.7.metres,
        line = listOf(GeoPoint(latitude = 49.933809, longitude = 14.1832917), GeoPoint(latitude = 50.5, longitude = 14.0)),
        attribution = "Map data from OpenStreetMap",
    )

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
