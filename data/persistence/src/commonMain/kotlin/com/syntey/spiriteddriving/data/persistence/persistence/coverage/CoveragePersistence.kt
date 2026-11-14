package com.syntey.spiriteddriving.data.persistence.persistence.coverage

import androidx.datastore.preferences.core.stringPreferencesKey
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.persistence.persistence.JsonPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Keeps the covered area as one JSON value, and its vignette countries under their own key, so reading the countries
 * doesn't decode the area. The countries are stored as a list of codes, which is the stored format.
 * An unreadable copy reads as `null`, so the app fetches it again.
 */
@Single
internal class CoveragePersistence(private val jsonPersistence: JsonPersistence) : CoverageCache {

    override suspend fun get(): VersionedCoverage? = jsonPersistence.getCatching<StoredCoverage>(KEY)?.toDomain()

    override fun observeVignetteCountries(): Flow<List<CountryCode>?> =
        jsonPersistence.observeCatching<List<String>>(VIGNETTE_COUNTRIES_KEY)
            .map { stored -> stored?.map(::CountryCode) ?: countriesFromCoverage() }
            .distinctUntilChanged()

    /**
     * Installs from before the countries had their own key keep their copy until the backend's covered area changes (it
     * answers 304 until then), so they'd otherwise never get the key. Reading the copy once stores the key for next time.
     */
    private suspend fun countriesFromCoverage(): List<CountryCode>? =
        get()?.coverage?.vignetteCountries?.also { countries ->
            jsonPersistence.save(VIGNETTE_COUNTRIES_KEY, countries.map { it.value })
        }

    override suspend fun save(coverage: VersionedCoverage) {
        jsonPersistence.save(KEY, coverage.toStored())
        jsonPersistence.save(VIGNETTE_COUNTRIES_KEY, coverage.coverage.vignetteCountries.map { it.value })
    }

    private companion object {
        val KEY = stringPreferencesKey("COVERAGE")
        val VIGNETTE_COUNTRIES_KEY = stringPreferencesKey("VIGNETTE_COUNTRIES")
    }
}
