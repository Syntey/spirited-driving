package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.persistence.database.CoveredPolygonEntity
import com.syntey.spiriteddriving.data.persistence.database.CoverageDao
import com.syntey.spiriteddriving.data.persistence.database.CoverageMetaEntity
import com.syntey.spiriteddriving.data.persistence.database.toEntity
import com.syntey.spiriteddriving.data.persistence.database.toStored
import com.syntey.spiriteddriving.data.persistence.injection.PersistenceJson
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

/**
 * Keeps the covered area in Room, one row per polygon and a version row, and its vignette countries under their own
 * DataStore key, so reading the countries doesn't read the area. An unreadable copy reads as `null`, so the app
 * fetches it again.
 */
@Single
internal class CoveragePersistence(
    private val jsonPersistence: JsonPersistence,
    private val coverageDao: CoverageDao,
    @PersistenceJson private val json: Json,
) : CoverageCache {

    private val logger = Logger.withTag("CoveragePersistence")

    override suspend fun get(): VersionedCoverage? {
        val rows = coverageDao.read()
        val vignetteCountries = rows?.let { jsonPersistence.getCatching<List<String>>(VIGNETTE_COUNTRIES_KEY) }
        val area = rows?.let { decodeArea(it.polygons) }
        return if (rows == null || vignetteCountries == null || area == null) {
            null
        } else {
            VersionedCoverage(
                coverage = Coverage(area = area, vignetteCountries = vignetteCountries.map(::CountryCode)),
                version = rows.meta.version,
            )
        }
    }

    /** `null` when a polygon can't be decoded, so the whole area is fetched again rather than shown in part. */
    private fun decodeArea(polygons: List<CoveredPolygonEntity>): List<GeoPolygon>? = try {
        polygons.map { it.toStored(json).toDomain() }
    } catch (error: SerializationException) {
        logger.e(error) { "Unable to decode the kept covered area" }
        null
    }

    // DataStore emits on every edit of any key; distinctUntilChanged keeps an edit elsewhere from re-emitting
    override fun observeVignetteCountries(): Flow<List<CountryCode>?> =
        jsonPersistence.observeCatching<List<String>>(VIGNETTE_COUNTRIES_KEY)
            .map { stored -> stored?.map(::CountryCode) }
            .distinctUntilChanged()

    override suspend fun save(coverage: VersionedCoverage) {
        val polygons = coverage.coverage.area.map { it.toStored().toEntity(json) }
        jsonPersistence.saveAndRemove(
            key = VIGNETTE_COUNTRIES_KEY,
            value = coverage.coverage.vignetteCountries.map { it.value },
            removedKey = LEGACY_COVERAGE_KEY,
        )
        // The version row is written last, so it's the commit marker: a copy whose countries were written but whose
        // area wasn't reads as the previous version, and the next fetch replaces both
        coverageDao.replace(polygons = polygons, meta = CoverageMetaEntity(version = coverage.version))
    }

    private companion object {
        val VIGNETTE_COUNTRIES_KEY = stringPreferencesKey("VIGNETTE_COUNTRIES")

        // The covered area used to be one JSON value under this key; it's removed on save and never read
        val LEGACY_COVERAGE_KEY = stringPreferencesKey("COVERAGE")
    }
}
