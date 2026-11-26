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
import com.syntey.spiriteddriving.data.persistence.database.CoveredPolygonEntity
import com.syntey.spiriteddriving.data.persistence.database.CoverageMetaEntity
import com.syntey.spiriteddriving.data.persistence.database.DatabaseTest
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CoveragePersistenceTest : DatabaseTest() {

    @Test
    fun `nothing kept reads as null`() = runTest {
        assertNull(coveragePersistence().get())
    }

    @Test
    fun `a kept copy with several polygons reads back equal with holes and version`() = runTest {
        val persistence = coveragePersistence()

        persistence.save(sampleCoverage())

        assertEquals(sampleCoverage(), persistence.get())
    }

    @Test
    fun `saving again replaces the previous copy`() = runTest {
        val persistence = coveragePersistence()
        val newer = sampleCoverage(area = listOf(samplePolygon(70.0))).copy(version = "\"newer\"")

        persistence.save(sampleCoverage())
        persistence.save(newer)

        assertEquals(newer, persistence.get())
    }

    @Test
    fun `saving writes the vignette countries key`() = runTest {
        val dataStore = InMemoryDataStore()

        coveragePersistence(dataStore).save(sampleCoverage())

        assertEquals("""["CZ","SK"]""", dataStore.data.first()[VIGNETTE_COUNTRIES_KEY])
    }

    @Test
    fun `nothing kept has no vignette countries`() = runTest {
        assertNull(coveragePersistence().observeVignetteCountries().first())
    }

    @Test
    fun `the vignette countries read back in the saved order`() = runTest {
        val persistence = coveragePersistence()

        persistence.save(sampleCoverage())

        assertEquals(listOf(CountryCode.Czechia, CountryCode("SK")), persistence.observeVignetteCountries().first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `saving a copy emits its vignette countries to an observer`() = runTest {
        val persistence = coveragePersistence()
        val emissions = mutableListOf<List<CountryCode>?>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            persistence.observeVignetteCountries().collect { emissions += it }
        }

        persistence.save(sampleCoverage())
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(null, listOf(CountryCode.Czechia, CountryCode("SK"))), emissions)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `an edit of another key doesn't read the kept copy again`() = runTest {
        val dataStore = InMemoryDataStore()
        val persistence = coveragePersistence(dataStore)
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            persistence.observeVignetteCountries().collect {}
        }
        runCurrent()
        val readsBefore = dataStore.reads

        dataStore.edit { it[OTHER_KEY] = "km" }
        dataStore.edit { it[OTHER_KEY] = "mi" }
        runCurrent()
        collecting.cancel()

        assertEquals(readsBefore, dataStore.reads)
    }

    @Test
    fun `an undecodable polygon reads as null`() = runTest {
        val database = database()
        val persistence = coveragePersistence(database = database)
        persistence.save(sampleCoverage())
        database.coverageDao().replace(
            polygons = listOf(
                CoveredPolygonEntity(
                    minLatitude = 0.0,
                    maxLatitude = 0.0,
                    minLongitude = 0.0,
                    maxLongitude = 0.0,
                    outline = "garbage",
                    holes = "[]",
                ),
            ),
            meta = CoverageMetaEntity(version = "\"v1\""),
        )

        assertNull(persistence.get())
    }

    @Test
    fun `a legacy COVERAGE key is ignored`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[LEGACY_COVERAGE_KEY] = LEGACY_COPY }

        assertNull(coveragePersistence(dataStore).get())
    }

    @Test
    fun `saving removes the legacy COVERAGE key`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.edit { it[LEGACY_COVERAGE_KEY] = LEGACY_COPY }

        coveragePersistence(dataStore).save(sampleCoverage())

        assertNull(dataStore.data.first()[LEGACY_COVERAGE_KEY])
    }

    private fun coveragePersistence(
        dataStore: DataStore<Preferences> = InMemoryDataStore(),
        database: PersistenceDatabase = database(),
    ): CoveragePersistence {
        val json = PersistenceModule().provideJson()
        return CoveragePersistence(JsonPersistence(dataStore, json), database.coverageDao(), json)
    }

    private fun sampleCoverage(area: List<GeoPolygon> = listOf(samplePolygon(50.0), samplePolygon(60.0))) = VersionedCoverage(
        coverage = Coverage(
            area = area,
            vignetteCountries = listOf(CountryCode.Czechia, CountryCode("SK")),
        ),
        version = "\"v1\"",
    )

    private fun samplePolygon(latitude: Double) = GeoPolygon(
        outline = listOf(
            GeoPoint(latitude, 12.0),
            GeoPoint(latitude, 13.0),
            GeoPoint(latitude + 1.0, 13.0),
            GeoPoint(latitude, 12.0),
        ),
        holes = listOf(
            listOf(
                GeoPoint(latitude + 0.2, 12.2),
                GeoPoint(latitude + 0.2, 12.4),
                GeoPoint(latitude + 0.4, 12.4),
                GeoPoint(latitude + 0.2, 12.2),
            ),
        ),
    )

    private companion object {
        const val LEGACY_COPY = """{"area":[],"vignetteCountries":["CZ"],"version":"v1"}"""
        val LEGACY_COVERAGE_KEY = stringPreferencesKey("COVERAGE")
        val VIGNETTE_COUNTRIES_KEY = stringPreferencesKey("VIGNETTE_COUNTRIES")
        val OTHER_KEY = stringPreferencesKey("OTHER")
    }

    private class InMemoryDataStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow(emptyPreferences())

        // How many times something started reading, such as a one-off read of the kept copy
        var reads = 0
            private set

        override val data: Flow<Preferences> = flow {
            reads++
            emitAll(preferences)
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }
}
