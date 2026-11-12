package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import org.koin.core.annotation.Single

/**
 * Keeps the covered area as one JSON value. An unreadable copy reads as `null`, so the app fetches it again.
 */
@Single
internal class CoveragePersistence(private val jsonPersistence: JsonPersistence) : CoverageCache {

    override suspend fun get(): VersionedCoverage? = jsonPersistence.getCatching<StoredCoverage>(KEY)?.toDomain()

    override suspend fun save(coverage: VersionedCoverage) {
        jsonPersistence.save(KEY, coverage.toStored())
    }

    private companion object {
        val KEY = stringPreferencesKey("COVERAGE")
    }
}
