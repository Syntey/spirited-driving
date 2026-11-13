package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CoveragePersistenceTest {

    @Test
    fun `nothing kept reads as null`() = runTest {
        assertNull(coveragePersistence().get())
    }

    @Test
    fun `a kept copy reads back equal`() = runTest {
        val persistence = coveragePersistence()

        persistence.save(sampleCoverage())

        assertEquals(sampleCoverage(), persistence.get())
    }

    @Test
    fun `saving again replaces the previous copy`() = runTest {
        val persistence = coveragePersistence()
        val newer = sampleCoverage().copy(version = "\"newer\"")

        persistence.save(sampleCoverage())
        persistence.save(newer)

        assertEquals(newer, persistence.get())
    }

    @Test
    fun `nothing kept has no vignette countries`() = runTest {
        assertNull(coveragePersistence().vignetteCountries())
    }

    @Test
    fun `the vignette countries read back in the saved order`() = runTest {
        val persistence = coveragePersistence()

        persistence.save(sampleCoverage())

        assertEquals(listOf(CountryCode.Czechia, CountryCode("SK")), persistence.vignetteCountries())
    }

    @Test
    fun `a kept copy without its vignette countries key reads the countries from the copy and keeps them`() = runTest {
        val dataStore = InMemoryDataStore()
        val persistence = coveragePersistence(dataStore)
        persistence.save(sampleCoverage())
        dataStore.edit { it.remove(VIGNETTE_COUNTRIES_KEY) }

        assertEquals(listOf(CountryCode.Czechia, CountryCode("SK")), persistence.vignetteCountries())
        assertNotNull(dataStore.data.first()[VIGNETTE_COUNTRIES_KEY])
    }

    @Test
    fun `an invalid copy reads as null`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[COVERAGE_KEY] = "garbage" }

        assertNull(coveragePersistence(dataStore).get())
    }

    @Test
    fun `a copy missing its fields reads as null`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[COVERAGE_KEY] = "{}" }

        assertNull(coveragePersistence(dataStore).get())
    }

    private fun coveragePersistence(dataStore: DataStore<Preferences> = InMemoryDataStore()) =
        CoveragePersistence(JsonPersistence(dataStore, PersistenceModule().provideJson()))

    private fun sampleCoverage() = VersionedCoverage(
        coverage = Coverage(
            area = listOf(
                GeoPolygon(
                    outline = listOf(GeoPoint(50.0, 12.0), GeoPoint(50.0, 13.0), GeoPoint(51.0, 13.0), GeoPoint(50.0, 12.0)),
                    holes = listOf(listOf(GeoPoint(50.2, 12.2), GeoPoint(50.2, 12.4), GeoPoint(50.4, 12.4), GeoPoint(50.2, 12.2))),
                ),
            ),
            vignetteCountries = listOf(CountryCode.Czechia, CountryCode("SK")),
        ),
        version = "\"v1\"",
    )

    private companion object {
        val COVERAGE_KEY = stringPreferencesKey("COVERAGE")
        val VIGNETTE_COUNTRIES_KEY = stringPreferencesKey("VIGNETTE_COUNTRIES")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = preferences

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
