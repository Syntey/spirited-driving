package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class InMemoryCoverageCache(private var stored: VersionedCoverage? = null) : CoverageCache {
    var saves = 0
        private set

    override suspend fun get(): VersionedCoverage? = stored

    override fun observeVignetteCountries(): Flow<List<CountryCode>?> = flowOf(stored?.coverage?.vignetteCountries)

    override suspend fun save(coverage: VersionedCoverage) {
        saves++
        stored = coverage
    }
}
